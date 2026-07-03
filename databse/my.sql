-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 68.178.237.26    Database: montfortug
-- ------------------------------------------------------
-- Server version	5.5.5-10.11.17-MariaDB-cll-lve

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `erp_academichistory`
--

DROP TABLE IF EXISTS `erp_academichistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_academichistory` (
  `HistoryID` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `FormerSchool` varchar(255) DEFAULT NULL,
  `LIN` varchar(100) DEFAULT NULL,
  `Combination` varchar(100) DEFAULT NULL,
  `PLEIndexNumber` varchar(100) DEFAULT NULL,
  `PLEAggregate` varchar(50) DEFAULT NULL,
  `UCEIndexNumber` varchar(100) DEFAULT NULL,
  `UCEResult` varchar(100) DEFAULT NULL,
  `FormerSchoolCode` varchar(50) DEFAULT NULL,
  `SubjectMarks` longtext DEFAULT NULL,
  `PreviousMarksDoc` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`HistoryID`),
  KEY `branch_id` (`branch_id`,`AdmissionNo`),
  CONSTRAINT `erp_academichistory_ibfk_1` FOREIGN KEY (`branch_id`, `AdmissionNo`) REFERENCES `erp_students` (`branch_id`, `AdmissionNo`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_alumni`
--

DROP TABLE IF EXISTS `erp_alumni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_alumni` (
  `alumni_id` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) DEFAULT NULL,
  `branch_id` int(11) DEFAULT NULL,
  `CompletionYear` varchar(50) DEFAULT NULL,
  `Notes` text DEFAULT NULL,
  PRIMARY KEY (`alumni_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_application_documents`
--

DROP TABLE IF EXISTS `erp_application_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_documents` (
  `document_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL,
  `document_type` varchar(50) NOT NULL,
  `original_file_name` varchar(255) DEFAULT NULL,
  `stored_file_name` varchar(255) DEFAULT NULL,
  `file_path` varchar(500) DEFAULT NULL,
  `uploaded_at` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`document_id`),
  KEY `fk_application_documents` (`application_id`),
  CONSTRAINT `fk_application_documents` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_application_status_history`
--

DROP TABLE IF EXISTS `erp_application_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_status_history` (
  `history_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL,
  `old_status` varchar(30) DEFAULT NULL,
  `new_status` varchar(30) NOT NULL,
  `changed_by` bigint(20) DEFAULT NULL,
  `changed_at` datetime DEFAULT current_timestamp(),
  `remarks` text DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `fk_application_history` (`application_id`),
  CONSTRAINT `fk_application_history` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_applications`
--

DROP TABLE IF EXISTS `erp_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_applications` (
  `application_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_no` varchar(50) NOT NULL,
  `branch_id` bigint(20) NOT NULL,
  `academic_year_id` bigint(20) NOT NULL,
  `branch_class_id` bigint(20) NOT NULL,
  `admission_type` varchar(20) DEFAULT 'NEW',
  `primary_email` varchar(100) DEFAULT NULL,
  `primary_mobile` varchar(20) DEFAULT NULL,
  `first_name` varchar(50) NOT NULL,
  `middle_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(50) DEFAULT 'Uganda',
  `photo_path` text DEFAULT NULL,
  `more_info` text DEFAULT NULL,
  `class_code` varchar(20) NOT NULL DEFAULT '',
  `level` varchar(20) NOT NULL DEFAULT '',
  `term` varchar(20) NOT NULL DEFAULT '',
  `date_of_registration` varchar(20) NOT NULL DEFAULT '',
  `scholarship_status` varchar(50) NOT NULL DEFAULT '',
  `previous_school` varchar(150) DEFAULT NULL,
  `former_school` text DEFAULT NULL,
  `former_school_code` varchar(50) NOT NULL DEFAULT '',
  `former_school_lin` varchar(50) NOT NULL DEFAULT '',
  `ple_ref` varchar(50) NOT NULL DEFAULT '',
  `ple_score` double DEFAULT NULL,
  `uce_ref` varchar(50) NOT NULL DEFAULT '',
  `uce_score` double DEFAULT NULL,
  `subject_marks` text DEFAULT NULL,
  `prev_marks_doc` text DEFAULT NULL,
  `father_name` varchar(50) NOT NULL DEFAULT '',
  `father_contact` varchar(20) NOT NULL DEFAULT '',
  `father_email` varchar(100) NOT NULL DEFAULT '',
  `father_occupation` text DEFAULT NULL,
  `father_education` varchar(50) NOT NULL DEFAULT '',
  `father_age` int(11) DEFAULT 0,
  `mother_name` varchar(50) NOT NULL DEFAULT '',
  `mother_contact` varchar(20) NOT NULL DEFAULT '',
  `mother_email` varchar(100) NOT NULL DEFAULT '',
  `mother_occupation` text DEFAULT NULL,
  `mother_education` varchar(50) NOT NULL DEFAULT '',
  `mother_age` int(11) DEFAULT 0,
  `guardian_name` varchar(50) NOT NULL DEFAULT '',
  `guardian_mobile` varchar(20) DEFAULT NULL,
  `guardian_contact` varchar(20) NOT NULL DEFAULT '',
  `guardian_email` varchar(100) DEFAULT NULL,
  `guardian_relation` varchar(50) NOT NULL DEFAULT '',
  `guardian_occupation` text DEFAULT NULL,
  `guardian_education` varchar(50) NOT NULL DEFAULT '',
  `guardian_location` text DEFAULT NULL,
  `guardian_age` int(11) DEFAULT 0,
  `address_region` varchar(50) NOT NULL DEFAULT '',
  `address_district` varchar(50) NOT NULL DEFAULT '',
  `address_village` varchar(50) NOT NULL DEFAULT '',
  `address_street` text DEFAULT NULL,
  `address_house` varchar(50) NOT NULL DEFAULT '',
  `address_postal` varchar(50) NOT NULL DEFAULT '',
  `application_status` varchar(50) DEFAULT 'DRAFT',
  `remarks` text DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp(),
  `status` int(11) DEFAULT 1,
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `uk_application_no` (`application_no`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_applications_legacy`
--

DROP TABLE IF EXISTS `erp_applications_legacy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_applications_legacy` (
  `app_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ref_number` varchar(50) NOT NULL,
  `branch_id` bigint(20) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `scholarship_status` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `academic_year` varchar(10) DEFAULT NULL,
  `term` varchar(20) DEFAULT NULL,
  `date_of_registration` varchar(50) DEFAULT NULL,
  `level` varchar(50) NOT NULL,
  `applied_class` varchar(50) NOT NULL,
  `class_code` varchar(10) NOT NULL,
  `student_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `student_surname` varchar(100) NOT NULL,
  `gender` varchar(10) NOT NULL,
  `dob` varchar(20) DEFAULT NULL,
  `nationality` varchar(100) DEFAULT 'Uganda',
  `photo_path` varchar(255) DEFAULT NULL,
  `address_country` varchar(100) DEFAULT 'Uganda',
  `address_state` varchar(100) DEFAULT NULL,
  `address_district` varchar(100) DEFAULT NULL,
  `address_village` varchar(100) DEFAULT NULL,
  `address_street` varchar(100) DEFAULT NULL,
  `address_house` varchar(100) DEFAULT NULL,
  `address_postal` varchar(20) DEFAULT NULL,
  `father_name` varchar(100) DEFAULT NULL,
  `father_age` int(11) DEFAULT NULL,
  `father_contact` varchar(50) DEFAULT NULL,
  `father_email` varchar(100) DEFAULT NULL,
  `father_occupation` varchar(100) DEFAULT NULL,
  `father_education` varchar(100) DEFAULT NULL,
  `mother_name` varchar(100) DEFAULT NULL,
  `mother_age` int(11) DEFAULT NULL,
  `mother_contact` varchar(50) DEFAULT NULL,
  `mother_email` varchar(100) DEFAULT NULL,
  `mother_occupation` varchar(100) DEFAULT NULL,
  `mother_education` varchar(100) DEFAULT NULL,
  `guardian_name` varchar(100) DEFAULT NULL,
  `guardian_relation` varchar(50) DEFAULT NULL,
  `guardian_age` int(11) DEFAULT NULL,
  `guardian_contact` varchar(50) DEFAULT NULL,
  `guardian_email` varchar(100) DEFAULT NULL,
  `guardian_occupation` varchar(100) DEFAULT NULL,
  `guardian_education` varchar(100) DEFAULT NULL,
  `former_school` varchar(255) DEFAULT NULL,
  `former_school_code` varchar(50) DEFAULT NULL,
  `former_school_lin` varchar(50) DEFAULT NULL,
  `ple_score` int(11) DEFAULT NULL,
  `ple_ref` varchar(50) DEFAULT NULL,
  `uce_score` int(11) DEFAULT NULL,
  `uce_ref` varchar(50) DEFAULT NULL,
  `subject_marks` longtext DEFAULT NULL,
  `prev_marks_doc` varchar(255) DEFAULT NULL,
  `more_info` text DEFAULT NULL,
  `guardian_location` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`app_id`),
  UNIQUE KEY `UK41obtiqpwhdoxlx32ywfxjw5b` (`ref_number`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_branch_fund_allocations`
--

DROP TABLE IF EXISTS `erp_branch_fund_allocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branch_fund_allocations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `donation_id` bigint(20) DEFAULT NULL,
  `amount_allocated_ugx` decimal(15,2) NOT NULL,
  `purpose` varchar(255) DEFAULT 'Branch Scholarship Pool',
  `academic_year` varchar(20) NOT NULL,
  `allocated_by_user_id` bigint(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `allocated_amount_ugx` decimal(38,2) NOT NULL,
  `term` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq42vuybrj74gyw9csy6angbis` (`donation_id`),
  CONSTRAINT `FKq42vuybrj74gyw9csy6angbis` FOREIGN KEY (`donation_id`) REFERENCES `web_donations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_branch_levels`
--

DROP TABLE IF EXISTS `erp_branch_levels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branch_levels` (
  `branch_level_id` int(11) NOT NULL AUTO_INCREMENT,
  `branch_id` int(11) NOT NULL,
  `level_id` int(11) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`branch_level_id`),
  UNIQUE KEY `uk_branch_level` (`branch_id`,`level_id`),
  UNIQUE KEY `UKsac1pi66it0u88um2v2yfkwrq` (`branch_id`,`level_id`),
  KEY `fk_branch_level_level` (`level_id`),
  CONSTRAINT `fk_branch_level_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_branch_level_level` FOREIGN KEY (`level_id`) REFERENCES `erp_levels` (`level_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_branches`
--

DROP TABLE IF EXISTS `erp_branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_branches` (
  `branch_id` int(11) NOT NULL AUTO_INCREMENT,
  `branch_name` varchar(255) NOT NULL,
  `school_code` varchar(10) DEFAULT NULL,
  `branch_location` varchar(255) DEFAULT NULL,
  `is_active` int(11) DEFAULT 1,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `contact_details` text DEFAULT NULL,
  `foundation_date` varchar(255) DEFAULT NULL,
  `gov_document_url` varchar(255) DEFAULT NULL,
  `incharge_details` text DEFAULT NULL,
  `school_photo_url` varchar(255) DEFAULT NULL,
  `branch_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_enrollment`
--

DROP TABLE IF EXISTS `erp_enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_enrollment` (
  `EnrollmentID` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `AcademicYear` varchar(50) DEFAULT NULL,
  `term` varchar(20) DEFAULT NULL,
  `Class` varchar(50) DEFAULT NULL,
  `Level` varchar(50) DEFAULT NULL,
  `Stream` varchar(50) DEFAULT NULL,
  `Residence` varchar(50) DEFAULT NULL,
  `EntryStatus` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`EnrollmentID`),
  KEY `branch_id` (`branch_id`,`AdmissionNo`),
  CONSTRAINT `erp_enrollment_ibfk_1` FOREIGN KEY (`branch_id`, `AdmissionNo`) REFERENCES `erp_students` (`branch_id`, `AdmissionNo`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_enrollmenthistory`
--

DROP TABLE IF EXISTS `erp_enrollmenthistory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_enrollmenthistory` (
  `log_id` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) DEFAULT NULL,
  `branch_id` int(11) DEFAULT NULL,
  `AcademicYear` varchar(50) DEFAULT NULL,
  `Level` varchar(50) DEFAULT NULL,
  `Class` varchar(50) DEFAULT NULL,
  `Term` varchar(50) DEFAULT NULL,
  `Stream` varchar(50) DEFAULT NULL,
  `Residence` varchar(50) DEFAULT NULL,
  `EntryStatus` varchar(50) DEFAULT NULL,
  `DateMoved` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_levels`
--

DROP TABLE IF EXISTS `erp_levels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_levels` (
  `level_id` int(11) NOT NULL AUTO_INCREMENT,
  `level_name` varchar(255) NOT NULL,
  `display_order` int(11) NOT NULL,
  `status` int(11) DEFAULT 1,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`level_id`),
  UNIQUE KEY `level_name` (`level_name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_parents`
--

DROP TABLE IF EXISTS `erp_parents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_parents` (
  `ParentId` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `father_name` varchar(100) DEFAULT NULL,
  `father_contact` varchar(50) DEFAULT NULL,
  `father_email` varchar(100) DEFAULT NULL,
  `father_age` int(11) DEFAULT NULL,
  `father_occupation` varchar(100) DEFAULT NULL,
  `father_education` varchar(100) DEFAULT NULL,
  `mother_name` varchar(100) DEFAULT NULL,
  `mother_contact` varchar(50) DEFAULT NULL,
  `mother_email` varchar(100) DEFAULT NULL,
  `mother_age` int(11) DEFAULT NULL,
  `mother_occupation` varchar(100) DEFAULT NULL,
  `mother_education` varchar(100) DEFAULT NULL,
  `guardian_name` varchar(100) DEFAULT NULL,
  `guardian_relation` varchar(50) DEFAULT NULL,
  `guardian_contact` varchar(50) DEFAULT NULL,
  `guardian_email` varchar(100) DEFAULT NULL,
  `guardian_age` int(11) DEFAULT NULL,
  `guardian_occupation` varchar(100) DEFAULT NULL,
  `guardian_education` varchar(100) DEFAULT NULL,
  `guardian_address` varchar(255) DEFAULT NULL,
  `MoreInformation` text DEFAULT NULL,
  PRIMARY KEY (`ParentId`),
  KEY `branch_id` (`branch_id`,`AdmissionNo`),
  CONSTRAINT `erp_parents_ibfk_1` FOREIGN KEY (`branch_id`, `AdmissionNo`) REFERENCES `erp_students` (`branch_id`, `AdmissionNo`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_scholarship_allocations`
--

DROP TABLE IF EXISTS `erp_scholarship_allocations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_allocations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `donation_id` bigint(20) NOT NULL,
  `amount_allocated_ugx` decimal(15,2) NOT NULL,
  `terms_covered` varchar(100) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `allocated_by_user_id` bigint(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `allocated_amount_ugx` decimal(38,2) NOT NULL,
  `term` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs8cxscphnnfi82r2h9153yyw3` (`donation_id`),
  CONSTRAINT `FKs8cxscphnnfi82r2h9153yyw3` FOREIGN KEY (`donation_id`) REFERENCES `web_donations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_scholarship_applications`
--

DROP TABLE IF EXISTS `erp_scholarship_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_applications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `student_id` varchar(50) DEFAULT NULL,
  `amount_requested_ugx` decimal(38,2) NOT NULL,
  `term_requested` varchar(50) NOT NULL,
  `category` varchar(100) NOT NULL,
  `status` varchar(50) DEFAULT 'Pending',
  `academic_year` varchar(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `application_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK484j4qcdg1w0qthelr4bwxbhy` (`application_id`),
  CONSTRAINT `FK484j4qcdg1w0qthelr4bwxbhy` FOREIGN KEY (`application_id`) REFERENCES `erp_applications_legacy` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `erp_school_classes`
--

DROP TABLE IF EXISTS `erp_school_classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_school_classes` (
  `class_id` int(11) NOT NULL AUTO_INCREMENT,
  `level_id` int(11) NOT NULL,
  `class_code` varchar(255) NOT NULL,
  `class_name` varchar(255) NOT NULL,
  `display_order` int(11) NOT NULL,
  `status` int(11) DEFAULT 1,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`class_id`),
  UNIQUE KEY `class_code` (`class_code`),
  KEY `fk_school_class_level` (`level_id`),
  CONSTRAINT `fk_school_class_level` FOREIGN KEY (`level_id`) REFERENCES `erp_levels` (`level_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-03 12:31:30
