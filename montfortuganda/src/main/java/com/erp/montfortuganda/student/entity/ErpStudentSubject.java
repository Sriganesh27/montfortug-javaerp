package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.ErpAcademicYear;
import com.erp.montfortuganda.school.entity.ErpSubject;
import com.erp.montfortuganda.school.entity.SchoolClass;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

/** Optional subject selected for a student's specific academic placement. */
@Entity
@DynamicUpdate
@Table(name = "erp_student_subjects")
public class ErpStudentSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_subject_id")
    private Long studentSubjectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private ErpStudentEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private ErpAcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private ErpSubject subject;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public Long getStudentSubjectId() { return studentSubjectId; }

    public void setStudentSubjectId(Long studentSubjectId) { this.studentSubjectId = studentSubjectId; }

    public ErpStudent getStudent() { return student; }

    public void setStudent(ErpStudent student) { this.student = student; }

    public ErpStudentEnrollment getEnrollment() { return enrollment; }

    public void setEnrollment(ErpStudentEnrollment enrollment) { this.enrollment = enrollment; }

    public Branch getBranch() { return branch; }

    public void setBranch(Branch branch) { this.branch = branch; }

    public ErpAcademicYear getAcademicYear() { return academicYear; }

    public void setAcademicYear(ErpAcademicYear academicYear) { this.academicYear = academicYear; }

    public SchoolClass getSchoolClass() { return schoolClass; }

    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public ErpSubject getSubject() { return subject; }

    public void setSubject(ErpSubject subject) { this.subject = subject; }

    public Boolean getActive() { return active; }

    public void setActive(Boolean active) { this.active = active; }

    public Long getVersion() { return version; }

    public void setVersion(Long version) { this.version = version; }

    public Long getCreatedBy() { return createdBy; }

    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getUpdatedBy() { return updatedBy; }

    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (active == null) active = true;
        if (version == null) version = 0L;
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
