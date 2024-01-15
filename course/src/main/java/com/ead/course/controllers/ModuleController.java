package com.ead.course.controllers;

import com.ead.course.dtos.ModuleDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
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
public class ModuleController {

    @Autowired
    ModuleService moduleService;

    @Autowired
    CourseService courseService;

    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<Object> saveModule(
            @RequestBody @Valid ModuleDto moduleDto,
            @PathVariable UUID courseId
    ) {
        log.debug("POST saveModule moduleDto {} and courseId received {} ", moduleDto, courseId);
        Optional<CourseModel> courseModelOptional = courseService.findById(courseId);
        if (courseModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found!");
        }
        ModuleModel moduleModel = new ModuleModel();
        BeanUtils.copyProperties(moduleDto, moduleModel);
        moduleModel.setCourse(courseModelOptional.get());
        moduleModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        moduleService.save(moduleModel);
        log.debug("POST saveModule moduleModel saved {} ", moduleModel.toString());
        log.info("Module saved successfully moduleId {} ", moduleModel.getModuleId());
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleModel);
    }

    @DeleteMapping("/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> deleteModule(
            @PathVariable UUID courseId, @PathVariable UUID moduleId
    ) {
        log.debug("DELETE deleteModule courseId {} and moduleId received {} ", courseId, moduleId);
        Optional<ModuleModel> moduleModelOptional = moduleService.findModuleIntoCourse(courseId, moduleId);
        if (moduleModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }
        moduleService.delete(moduleModelOptional.get());
        log.debug("DELETE deleteModule moduleId deleted {} ", moduleId);
        log.info("Module deleted successfully moduleId {} ", moduleId);
        return ResponseEntity.status(HttpStatus.OK).body("Module deleted successfully");
    }

    @PutMapping("/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> updateModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @RequestBody @Valid ModuleDto moduleDto
    ) {
        log.debug("PUT updateModule courseId {} and moduleId {} and moduleDto received {} ", courseId, moduleId, moduleDto);
        Optional<ModuleModel> moduleModelOptional = moduleService.findModuleIntoCourse(courseId, moduleId);
        if (moduleModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found for this course");
        }
        ModuleModel moduleModel = moduleModelOptional.get();
        moduleModel.setTitle(moduleDto.getTitle());
        moduleModel.setDescription(moduleDto.getDescription());
        moduleService.save(moduleModel);
        log.debug("PUT updateModule moduleId updated {} ", moduleId);
        log.info("Module updated successfully moduleId {} ", moduleId);
        return ResponseEntity.status(HttpStatus.OK).body(moduleModel);
    }

    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<Page<ModuleModel>> getAllModules(
            @PathVariable UUID courseId,
            @PageableDefault(page = 0, size = 10, sort = "moduleId", direction = Sort.Direction.ASC) Pageable pageable,
            SpecificationTemplate.ModuleSpec spec
    ) {
        log.debug("GET getAllModules courseId {} and pageable {} and spec received {} ", courseId, pageable, spec);
        return ResponseEntity.status(HttpStatus.OK).body(moduleService.findAllByCourse(
                SpecificationTemplate.moduleCourseId(courseId).and(spec), pageable));
    }

    @GetMapping("/courses/{courseId}/modules/{moduleId}")
    public ResponseEntity<Object> getOneModule(
            @PathVariable UUID courseId, @PathVariable UUID moduleId
    ) {
        log.debug("GET getOneModule courseId {} and moduleId received {} ", courseId, moduleId);
        Optional<ModuleModel> moduleModelOptional = moduleService.findModuleIntoCourse(courseId, moduleId);
        if (moduleModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Course not found!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(moduleModelOptional.get());
    }
}
