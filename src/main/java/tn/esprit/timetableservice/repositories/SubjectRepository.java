package tn.esprit.timetableservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.timetableservice.entities.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
