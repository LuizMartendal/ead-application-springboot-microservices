package com.ead.course.services.impl;

import com.ead.course.dtos.NotificationCommandDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.models.UserModel;
import com.ead.course.publisher.NotificationCommandPublisher;
import com.ead.course.repositories.CourseRepository;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.repositories.UserRepository;
import com.ead.course.services.CourseService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
public class CourseServiceImpl implements CourseService {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NotificationCommandPublisher notificationCommandPublisher;

    @Transactional
    @Override
    public void delete(CourseModel course) {
        List<ModuleModel> moduleModelList = moduleRepository.findAllModulesIntoCourse(course.getCourseId());
        if (!moduleModelList.isEmpty()) {
            for (ModuleModel m : moduleModelList) {
                List<LessonModel> lessonModelList = lessonRepository.findAllLessonsIntoModule(m.getModuleId());
                if (!lessonModelList.isEmpty()) {
                    lessonRepository.deleteAll(lessonModelList);
                }
            }
            moduleRepository.deleteAll(moduleModelList);
        }
        courseRepository.deleteCourseUserByCourse(course.getCourseId());
        courseRepository.delete(course);
    }

    @Override
    public CourseModel save(CourseModel course) {
        return courseRepository.save(course);
    }

    @Override
    public Optional<CourseModel> findById(UUID courseId) {
        return courseRepository.findById(courseId);
    }

    @Override
    public Page<CourseModel> findAll(Specification<CourseModel> spec, Pageable pageable) {
        return courseRepository.findAll(spec, pageable);
    }

    @Override
    public boolean existsByCourseAndUser(UUID courseId, UUID userId) {
        return courseRepository.existsByCourseAndUser(courseId, userId);
    }

    @Transactional
    @Override
    public void saveSubscriptionUserInCourse(UUID courseId, UUID userId) {
        courseRepository.saveCourseUser(courseId, userId);
    }

    @Transactional
    @Override
    public void saveSubscriptionUserInCourseAndSendNotification(CourseModel courseModel, UserModel userModel) {
        courseRepository.saveCourseUser(courseModel.getCourseId(), userModel.getUserId());
        try {
            NotificationCommandDto notificationCommandDto = new NotificationCommandDto();
            notificationCommandDto.setTitle("Bem vindo(a) ao Curso: " + courseModel.getName());
            notificationCommandDto.setMessage(userModel.getFullName() + " a sua inscrição foi realizada com sucesso!");
            notificationCommandDto.setUserId(userModel.getUserId());
            notificationCommandPublisher.publishNotificationCommand(notificationCommandDto);
        } catch (Exception e) {
            log.warn("Error sending notification!");
        }
    }
}
