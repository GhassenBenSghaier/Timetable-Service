package tn.esprit.timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import tn.esprit.timetableservice.entities.Teacher;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

}
