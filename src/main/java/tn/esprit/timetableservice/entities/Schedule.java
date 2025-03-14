package tn.esprit.timetableservice.entities;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//@Entity
@Getter
@Setter
@AllArgsConstructor
//@NoArgsConstructor
@Entity


public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonManagedReference
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonManagedReference
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonManagedReference
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonManagedReference
    private Subject subject;

    private String timeSlot;
    private String day;

    public Schedule() {}
    public Schedule(Teacher teacher, Classroom classroom, Subject subject, SchoolClass schoolClass, String day, String timeSlot) {
        this.teacher = teacher;
        this.classroom = classroom;
        this.subject = subject;
        this.schoolClass = schoolClass;
        this.day = day;
        this.timeSlot = timeSlot;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }
    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
}