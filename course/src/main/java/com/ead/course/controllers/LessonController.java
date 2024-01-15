package com.ead.course.controllers;

import com.ead.course.dtos.LessonDto;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.LessonService;
import com.ead.course.services.ModuleService;
import com.ead.course.specifications.SpecificationTemplate;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class LessonController {

    @Autowired
    LessonService lessonService;

    @Autowired
    ModuleService moduleService;

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Object> saveLesson(
            @PathVariable UUID moduleId, @RequestBody @Valid LessonDto lessonDto
    ) {
        log.debug("POST saveLesson moduleId {} and lessonDto received {} ", moduleId, lessonDto);
        Optional<ModuleModel> moduleModelOptional = moduleService.findById(moduleId);
        if (moduleModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found!");
        }
        LessonModel lessonModel = new LessonModel();
        BeanUtils.copyProperties(lessonDto, lessonModel);
        lessonModel.setModule(moduleModelOptional.get());
        lessonModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        log.debug("POST saveLesson lessonModel saved {} ", lessonModel.toString());
        log.info("Lesson saved successfully lessonId {} ", lessonModel.getLessonId());
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.save(lessonModel));
    }

    @DeleteMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<Object> deleteLesson(
            @PathVariable UUID moduleId, @PathVariable UUID lessonId
    ) {
        log.debug("DELETE deleteLesson moduleId {} and lessonId received {} ", moduleId, lessonId);
        Optional<LessonModel> lessonModelOptional = lessonService.findLessonIntoModule(moduleId, lessonId);
        if (lessonModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lesson not found for this module!");
        }
        lessonService.delete(lessonModelOptional.get());
        log.debug("DELETE deleteLesson lessonId deleted {} ", lessonId);
        log.info("Lesson deleted successfully lessonId {} ", lessonId);
        return ResponseEntity.status(HttpStatus.OK).body("Lesson deleted successfully");
    }

    @PutMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<Object> updateLesson(
            @PathVariable UUID moduleId, @PathVariable UUID lessonId,
            @RequestBody @Valid LessonDto lessonDto
    ) {
        log.debug("PUT updateLesson moduleId {} and lessonId {} and lessonDto received {} ", moduleId, lessonId, lessonDto);
        Optional<LessonModel> lessonModelOptional = lessonService.findLessonIntoModule(moduleId, lessonId);
        if (lessonModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lesson not found for this module");
        }
        LessonModel lessonModel = lessonModelOptional.get();
        lessonModel.setTitle(lessonDto.getTitle());
        lessonModel.setDescription(lessonDto.getDescription());
        lessonModel.setVideoUrl(lessonDto.getVideoUrl());
        log.debug("PUT updateLesson lessonId {} updated ", lessonId);
        log.info("Lesson updated successfully lessonId {} ", lessonId);
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.save(lessonModel));
    }

    @GetMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Page<LessonModel>> getAllLessonsByModule(
            @PathVariable UUID moduleId,
            SpecificationTemplate.LessonSpec spec,
            @PageableDefault(page = 0, size = 10, sort = "lessonId", direction = Sort.Direction.ASC) Pageable pageable
            ) {
        log.debug("GET getAllLessonsByModule moduleId {} and spec {} and pageable received {} ", moduleId, spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(lessonService.findAllLessonsIntoModule(SpecificationTemplate.lessonModuleId(moduleId).and(spec), pageable));
    }

    @GetMapping("/modules/{moduleId}/lessons/{lessonId}")
    public ResponseEntity<Object> getOneLesson(
            @PathVariable UUID moduleId, @PathVariable UUID lessonId
    ) {
        log.debug("GET getOneLesson moduleId {} and lessonId received {} ", moduleId, lessonId);
        Optional<LessonModel> lessonModelOptional = lessonService.findLessonIntoModule(moduleId, lessonId);
        if (lessonModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lesson not found for this module!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(lessonModelOptional.get());
    }
}
