package com.example.investigation_case_service.service.impl;

import com.example.investigation_case_service.dto.request.CreateCaseRequest;
import com.example.investigation_case_service.dto.request.MemberRequest;
import com.example.investigation_case_service.dto.request.UpdateCaseRequest;
import com.example.investigation_case_service.dto.response.AuditEventResponse;
import com.example.investigation_case_service.dto.response.CaseFileResponse;
import com.example.investigation_case_service.dto.response.CaseResponse;
import com.example.investigation_case_service.dto.response.MemberResponse;
import com.example.investigation_case_service.exception.ApiException;
import com.example.investigation_case_service.model.cases.AuditEvent;
import com.example.investigation_case_service.model.cases.Case;
import com.example.investigation_case_service.model.cases.CaseFileRecord;
import com.example.investigation_case_service.model.cases.CaseMember;
import com.example.investigation_case_service.model.cases.CaseMemberId;
import com.example.investigation_case_service.model.cases.FileAccess;
import com.example.investigation_case_service.model.enums.cases.AuditEventType;
import com.example.investigation_case_service.model.enums.cases.CaseStatus;
import com.example.investigation_case_service.model.enums.users.MemberType;
import com.example.investigation_case_service.model.enums.users.Role;
import com.example.investigation_case_service.model.users.User;
import com.example.investigation_case_service.repository.AuditEventRepository;
import com.example.investigation_case_service.repository.CaseFileRecordRepository;
import com.example.investigation_case_service.repository.CaseMemberRepository;
import com.example.investigation_case_service.repository.CaseRepository;
import com.example.investigation_case_service.repository.FileAccessRepository;
import com.example.investigation_case_service.repository.UserRepository;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.CaseService;
import com.example.investigation_case_service.util.Pages;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {
    private static final String NO_ACCESS = "Нет доступа к делу";

    private final CaseRepository caseRepository;
    private final CaseMemberRepository caseMemberRepository;
    private final CaseFileRecordRepository caseFileRecordRepository;
    private final FileAccessRepository fileAccessRepository;
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Value("${app.seed.case-files:}")
    private List<String> caseFiles;

    @Override
    @Transactional
    public CaseResponse createCase(CreateCaseRequest requset, AppUserPrincipal user) {
        String number = requset.getNumber().trim();
        String title = requset.getTitle().trim();
        if (caseRepository.existsByNumber(number)) {
            log.info("[{}] case is already exists.", number);
            throw ApiException.conflict("Дело с номером " + number + " уже существует");
        }
        User owner = currentUser(user);

        Case c = new Case();
        c.setNumber(number);
        c.setTitle(title);
        c.setDescription(requset.getDescription());
        c.setStatus(CaseStatus.OPEN);
        c.setOwner(owner);
        c.setRegion(owner.getRegion());
        caseRepository.save(c);


        if (caseFiles != null) {
            for (String name : caseFiles) {
                if (name != null && !name.isBlank()) {
                    CaseFileRecord f = new CaseFileRecord();
                    f.setName(name.trim());
                    f.setCaseEntity(c);
                    caseFileRecordRepository.save(f);
                }
            }
        }

        log(c.getId(), owner, AuditEventType.CASE_CREATED, "CASE", c.getId(), null,
                "number=" + number + ", status=" + c.getStatus());
        return CaseResponse.from(c);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseResponse> listCases(AppUserPrincipal user, int page, int size) {
        Pageable pageable = Pages.of(page, size);
        Page<Case> result;
        if (user.role() == Role.SYSTEM_ADMIN) {
            result = caseRepository.findAllDetailed(pageable);
        } else if (user.role() == Role.REGIONAL_ADMIN) {
            result = caseRepository.findAllByRegion(user.regionId(), pageable);
        } else {
            result = caseRepository.findAccessibleTo(user.id(), pageable);
        }
        return result.map(CaseResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public CaseResponse getCase(Long caseId, AppUserPrincipal user) {
        Case c = findCase(caseId);
        boolean canView = isOwner(c, user) || isSupervisor(c, user)
                || member(c, user).map(CaseMember::isCanView).orElse(false);
        if (!canView) {
            log.info("User [{}] has no access to case [{}]", user.id(), caseId);
            throw ApiException.forbidden(NO_ACCESS);
        }
        return CaseResponse.from(c);
    }

    @Override
    @Transactional
    public CaseResponse updateCase(Long caseId, UpdateCaseRequest requset, AppUserPrincipal user) {
        Case c = findCase(caseId);
        boolean owner = isOwner(c, user);
        boolean editor = owner || member(c, user).map(CaseMember::isCanEdit).orElse(false);
        if (!editor) {
            log.info("[{}] case is not owner or supervisor", caseId);
            throw ApiException.forbidden("Нет права на редактирование дела");
        }
        if (requset == null) {
            log.info("Empty request");
            throw ApiException.badRequest("Пустой запрос");
        }
        if (requset.getStatus() != null && requset.getStatus() != c.getStatus() && !owner) {
            log.info("The status of the case is changed only by the owner");
            throw ApiException.forbidden("Статус дела меняет только владелец");
        }
        User actor = currentUser(user);
        boolean changed = false;

        if (requset.getTitle() != null && !requset.getTitle().trim().equals(c.getTitle())) {
            String title = requset.getTitle().trim();
            if (title.isEmpty() || title.length() > 255) {
                throw ApiException.badRequest("Название не может быть пустым или длиннее 255 символов");
            }
            log(c.getId(), actor, AuditEventType.CASE_UPDATED, "CASE", c.getId(),
                    "title=" + c.getTitle(), "title=" + title);
            c.setTitle(title);
            changed = true;
        }
        if (requset.getDescription() != null && !requset.getDescription().equals(c.getDescription())) {
            log(c.getId(), actor, AuditEventType.CASE_UPDATED, "CASE", c.getId(),
                    "description=" + c.getDescription(), "description=" + requset.getDescription());
            c.setDescription(requset.getDescription());
            changed = true;
        }
        if (requset.getStatus() != null && requset.getStatus() != c.getStatus()) {
            log(c.getId(), actor, AuditEventType.STATUS_CHANGED, "CASE", c.getId(),
                    c.getStatus().name(), requset.getStatus().name());
            c.setStatus(requset.getStatus());
            changed = true;
        }
        if (changed) {
            c.setUpdatedAt(Instant.now());
        }
        return CaseResponse.from(c);
    }

    @Override
    @Transactional
    public MemberResponse putMember(Long caseId, Long userId, MemberRequest requset, AppUserPrincipal user) {
        Case c = findCase(caseId);
        requireOwner(c, user);
        if (requset == null || requset.getMemberType() == null) {
            throw ApiException.badRequest("Обязателен memberType: PARTICIPANT или TASK_FORCE_MEMBER");
        }
        if (userId.equals(c.getOwner().getId())) {
            throw ApiException.badRequest("Владелец дела не может быть его участником");
        }

        User target = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
        if (!target.isEnabled()) {
            log.info("User [{}] is locked", target.getLogin());
            throw ApiException.badRequest("Учётная запись пользователя отключена");
        }
        User actor = currentUser(user);
        boolean canView = Boolean.TRUE.equals(requset.getCanView());
        boolean canEdit = Boolean.TRUE.equals(requset.getCanEdit());

        List<FileAccess> currentAccess = fileAccessRepository.findAllByUserAndCase(userId, caseId);
        Set<Long> oldFileIds = currentAccess.stream()
                .map(a -> a.getFile().getId())
                .collect(Collectors.toCollection(TreeSet::new));
        Set<Long> newFileIds = oldFileIds;

        if (requset.getFileIds() != null) {
            Map<Long, CaseFileRecord> caseFiles = caseFileRecordRepository.findAllByCase(caseId).stream()
                    .collect(Collectors.toMap(CaseFileRecord::getId, f -> f));
            newFileIds = new TreeSet<>();
            for (Long fileId : new LinkedHashSet<>(requset.getFileIds())) {
                if (fileId == null || !caseFiles.containsKey(fileId)) {
                    throw ApiException.badRequest("Файл " + fileId + " не относится к этому делу");
                }
                newFileIds.add(fileId);
            }
            for (FileAccess a : currentAccess) {
                if (!newFileIds.contains(a.getFile().getId())) {
                    fileAccessRepository.delete(a);
                }
            }
            for (Long fileId : newFileIds) {
                if (!oldFileIds.contains(fileId)) {
                    FileAccess a = new FileAccess();
                    a.setFile(caseFiles.get(fileId));
                    a.setUser(target);
                    fileAccessRepository.save(a);
                }
            }
        }

        Optional<CaseMember> existing = caseMemberRepository.findById(new CaseMemberId(caseId, userId));
        CaseMember m;
        String newState = describe(requset.getMemberType(), canView, canEdit, newFileIds);
        if (existing.isPresent()) {
            m = existing.get();
            String oldState = describe(m.getMemberType(), m.isCanView(), m.isCanEdit(), oldFileIds);
            m.setMemberType(requset.getMemberType());
            m.setCanView(canView);
            m.setCanEdit(canEdit);
            m.setUpdatedAt(Instant.now());
            if (!oldState.equals(newState)) {
                log(caseId, actor, AuditEventType.ACCESS_CHANGED, "MEMBER", userId, oldState, newState);
            }
        } else {
            m = new CaseMember();
            m.setId(new CaseMemberId(caseId, userId));
            m.setCaseEntity(c);
            m.setUser(target);
            m.setMemberType(requset.getMemberType());
            m.setCanView(canView);
            m.setCanEdit(canEdit);
            m.setGrantedBy(actor);
            entityManager.persist(m);
            log(caseId, actor, AuditEventType.ACCESS_GRANTED, "MEMBER", userId, null, newState);
        }
        return new MemberResponse(caseId, userId, target.getLogin(), m.getMemberType(),
                m.isCanView(), m.isCanEdit(), List.copyOf(newFileIds), m.getGrantedAt(), m.getUpdatedAt());
    }

    @Override
    @Transactional
    public void removeMember(Long caseId, Long userId, AppUserPrincipal user) {
        Case c = findCase(caseId);
        requireOwner(c, user);
        CaseMember m = caseMemberRepository.findById(new CaseMemberId(caseId, userId))
                .orElseThrow(() -> ApiException.notFound("Пользователь не является участником дела"));

        List<FileAccess> access = fileAccessRepository.findAllByUserAndCase(userId, caseId);
        Set<Long> fileIds = access.stream()
                .map(a -> a.getFile().getId())
                .collect(Collectors.toCollection(TreeSet::new));
        String oldState = describe(m.getMemberType(), m.isCanView(), m.isCanEdit(), fileIds);

        fileAccessRepository.deleteAll(access);
        caseMemberRepository.delete(m);
        log(caseId, currentUser(user), AuditEventType.ACCESS_REVOKED, "MEMBER", userId, oldState, null);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CaseFileResponse> listFiles(Long caseId, AppUserPrincipal user) {
        Case c = findCase(caseId);
        List<CaseFileRecord> visible;
        if (isOwner(c, user) || isSupervisor(c, user)) {
            visible = caseFileRecordRepository.findAllByCase(caseId);
        } else if (member(c, user).map(CaseMember::isCanView).orElse(false)) {
            visible = caseFileRecordRepository.findAllowedByCaseAndUser(caseId, user.id());
        } else {
            throw ApiException.forbidden(NO_ACCESS);
        }
        return visible.stream()
                .map(f -> new CaseFileResponse(f.getId(), f.getName(), c.getNumber()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEventResponse> history(Long caseId, AppUserPrincipal user, int page, int size) {
        Case c = findCase(caseId);
        if (!isOwner(c, user) && !isSupervisor(c, user)) {
            throw ApiException.forbidden("Нет доступа к журналу дела");
        }
        return auditEventRepository.findAllByCase(caseId, Pages.of(page, size)).map(AuditEventResponse::from);
    }


    private Case findCase(Long caseId) {
        return caseRepository.findDetailedById(caseId)
                .orElseThrow(() -> ApiException.notFound("Дело не найдено"));
    }

    private User currentUser(AppUserPrincipal user) {
        return userRepository.findWithRegionById(user.id())
                .orElseThrow(() -> ApiException.forbidden("Пользователь не найден"));
    }

    private boolean isOwner(Case c, AppUserPrincipal user) {
        return c.getOwner().getId().equals(user.id());
    }

    private boolean isSupervisor(Case c, AppUserPrincipal user) {
        return user.role() == Role.SYSTEM_ADMIN
                || (user.role() == Role.REGIONAL_ADMIN && c.getRegion().getId().equals(user.regionId()));
    }

    private Optional<CaseMember> member(Case c, AppUserPrincipal user) {
        return caseMemberRepository.findById(new CaseMemberId(c.getId(), user.id()));
    }

    private void requireOwner(Case c, AppUserPrincipal user) {
        if (!isOwner(c, user)) {
            throw ApiException.forbidden("Управлять доступом может только владелец дела");
        }
    }

    private void log(Long caseId, User actor, AuditEventType type, String objectType, Long objectId,
                     String oldValue, String newValue) {
        AuditEvent e = new AuditEvent();
        e.setCaseId(caseId);
        e.setActor(actor);
        e.setEventType(type);
        e.setObjectType(objectType);
        e.setObjectId(String.valueOf(objectId));
        e.setOldValue(oldValue);
        e.setNewValue(newValue);
        auditEventRepository.save(e);
    }

    private static String describe(MemberType type, boolean canView, boolean canEdit, Collection<Long> fileIds) {
        return "memberType=" + type + ", canView=" + canView + ", canEdit=" + canEdit + ", fileIds=" + fileIds;
    }

}
