package tn.esprit.timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.timetableservice.entities.Classroom;


public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
}
