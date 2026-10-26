package com.learn.hub.position;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户的学习位置接口，需要 JWT。
 */
@RestController
@RequestMapping("/api/v1/me/positions")
public class StudyPositionController {

    private final StudyPositionService studyPositionService;

    public StudyPositionController(StudyPositionService studyPositionService) {
        this.studyPositionService = studyPositionService;
    }

    /**
     * 读取位置；没有记录时返回 204。
     */
    @GetMapping("/{scope}")
    public ResponseEntity<PositionDtos.PositionView> get(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String scope
    ) {
        return studyPositionService.get(userId, scope)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * 保存或覆盖位置。
     */
    @PutMapping("/{scope}")
    public PositionDtos.PositionView save(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String scope,
            @RequestBody PositionDtos.PositionRequest request
    ) {
        return studyPositionService.save(userId, scope, request);
    }

    /**
     * 清除位置记录。
     */
    @DeleteMapping("/{scope}")
    public ResponseEntity<Void> delete(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String scope
    ) {
        studyPositionService.delete(userId, scope);
        return ResponseEntity.noContent().build();
    }
}
