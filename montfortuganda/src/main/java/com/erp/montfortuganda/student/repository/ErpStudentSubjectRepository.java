package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentSubject;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ErpStudentSubjectRepository extends JpaRepository<ErpStudentSubject, Long> {

    @Query("""
            select ss from ErpStudentSubject ss
            join fetch ss.subject
            where ss.student.studentId = :studentId
              and ss.branch.branchId = :branchId
              and ss.academicYear.academicYearId = :academicYearId
              and ss.schoolClass.classId = :classId
            order by ss.subject.displayOrder asc, ss.subject.subjectName asc
            """)
    List<ErpStudentSubject> findPlacementRecords(
            @Param("studentId") Long studentId,
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("classId") Integer classId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ss from ErpStudentSubject ss
            where ss.student.studentId = :studentId
              and ss.branch.branchId = :branchId
              and ss.academicYear.academicYearId = :academicYearId
              and ss.schoolClass.classId = :classId
            """)
    List<ErpStudentSubject> lockPlacementRecords(
            @Param("studentId") Long studentId,
            @Param("branchId") Integer branchId,
            @Param("academicYearId") Long academicYearId,
            @Param("classId") Integer classId);

    Optional<ErpStudentSubject> findByStudent_StudentIdAndAcademicYear_AcademicYearIdAndSchoolClass_ClassIdAndSubject_SubjectId(
            Long studentId, Long academicYearId, Integer classId, Long subjectId);
}
