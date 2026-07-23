-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 68.178.237.26    Database: montfortug
-- ------------------------------------------------------
-- Server version	5.5.5-10.11.18-MariaDB-cll-lve

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
-- Table structure for table `erp_academic_terms`
--

DROP TABLE IF EXISTS `erp_academic_terms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_academic_terms` (
  `term_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `academic_year_id` bigint(20) NOT NULL,
  `term_code` varchar(20) NOT NULL,
  `term_name` varchar(100) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `display_order` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PLANNED',
  `current_term` tinyint(1) NOT NULL DEFAULT 0,
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`term_id`),
  UNIQUE KEY `uk_year_term_code` (`academic_year_id`,`term_code`),
  CONSTRAINT `fk_term_year` FOREIGN KEY (`academic_year_id`) REFERENCES `erp_academic_years` (`academic_year_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_academic_terms`
--

LOCK TABLES `erp_academic_terms` WRITE;
/*!40000 ALTER TABLE `erp_academic_terms` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_academic_terms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_academic_years`
--

DROP TABLE IF EXISTS `erp_academic_years`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_academic_years` (
  `academic_year_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `academic_year_code` varchar(20) NOT NULL,
  `academic_year_name` varchar(100) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `admission_start_date` date DEFAULT NULL,
  `admission_end_date` date DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PLANNED',
  `current_year` tinyint(1) NOT NULL DEFAULT 0,
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`academic_year_id`),
  UNIQUE KEY `uk_academic_year_code` (`academic_year_code`),
  CONSTRAINT `chk_academic_year_status` CHECK (`status` in ('PLANNED','ACTIVE','CLOSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_academic_years`
--

LOCK TABLES `erp_academic_years` WRITE;
/*!40000 ALTER TABLE `erp_academic_years` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_academic_years` ENABLE KEYS */;
UNLOCK TABLES;

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
  `verification_status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `original_file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `uploaded_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL,
  `file_size` bigint(20) DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_hash` varchar(64) DEFAULT NULL,
  `uploaded_by` bigint(20) DEFAULT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`document_id`),
  KEY `idx_application_document_branch_status` (`verification_status`),
  KEY `idx_application_document_application_status` (`application_id`,`verification_status`),
  CONSTRAINT `fk_application_document_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_application_documents`
--

LOCK TABLES `erp_application_documents` WRITE;
/*!40000 ALTER TABLE `erp_application_documents` DISABLE KEYS */;
INSERT INTO `erp_application_documents` VALUES (1,1,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782899073071_aa106764.jpeg','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-001/photo_1782899073071_aa106764.jpeg','2026-07-01 09:44:33',NULL,NULL,NULL,NULL,NULL,0,1),(2,2,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782899658052_258e0834.jpeg','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-002/photo_1782899658052_258e0834.jpeg','2026-07-01 09:54:18',NULL,NULL,NULL,NULL,NULL,0,1),(3,3,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782900179290_a1efd19b.jpeg','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-001/photo_1782900179290_a1efd19b.jpeg','2026-07-01 10:02:59',NULL,NULL,NULL,NULL,NULL,0,1),(4,4,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782900439677_98a61d45.jpeg','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-001/photo_1782900439677_98a61d45.jpeg','2026-07-01 10:07:19',NULL,NULL,NULL,NULL,NULL,0,1),(5,5,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782901940649_7964978a.jpeg','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-003/photo_1782901940649_7964978a.jpeg','2026-07-01 10:32:20',NULL,NULL,NULL,NULL,NULL,0,1),(6,5,'DOCUMENT','PENDING','Montfort_Application_APP-U031-2026-0001.pdf','doc_1782901940666_2579778c.pdf','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-003/doc_1782901940666_2579778c.pdf','2026-07-01 10:32:20',NULL,NULL,NULL,NULL,NULL,0,1),(7,6,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782907247078_26d0e9c4.jpeg','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-002/photo_1782907247078_26d0e9c4.jpeg','2026-07-01 12:00:47',NULL,NULL,NULL,NULL,NULL,0,1),(8,6,'DOCUMENT','PENDING','Montfort_Application_APP-U031-2026-0001.pdf','doc_1782907247101_64b18ed3.pdf','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-002/doc_1782907247101_64b18ed3.pdf','2026-07-01 12:00:47',NULL,NULL,NULL,NULL,NULL,0,1),(9,7,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782908680940_e24faa98.jpeg','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-004/photo_1782908680940_e24faa98.jpeg','2026-07-01 12:24:40',NULL,NULL,NULL,NULL,NULL,0,1),(10,7,'DOCUMENT','PENDING','bharath resume (2).pdf','doc_1782908680956_0f8b26f7.pdf','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-004/doc_1782908680956_0f8b26f7.pdf','2026-07-01 12:24:40',NULL,NULL,NULL,NULL,NULL,0,1),(11,8,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782909458376_86e95df1.jpeg','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-005/photo_1782909458376_86e95df1.jpeg','2026-07-01 12:37:38',NULL,NULL,NULL,NULL,NULL,0,1),(12,8,'DOCUMENT','PENDING','Montfort_Application_APP-U031-2026-0001.pdf','doc_1782909458395_1f0a7a1f.pdf','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-005/doc_1782909458395_1f0a7a1f.pdf','2026-07-01 12:37:38',NULL,NULL,NULL,NULL,NULL,0,1),(13,9,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782912293668_5a468c1d.jpeg','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-002/photo_1782912293668_5a468c1d.jpeg','2026-07-01 13:24:53',NULL,NULL,NULL,NULL,NULL,0,1),(14,9,'DOCUMENT','PENDING','Montfort_Application_APP-U031-2026-0001.pdf','doc_1782912293672_bb65484d.pdf','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-002/doc_1782912293672_bb65484d.pdf','2026-07-01 13:24:53',NULL,NULL,NULL,NULL,NULL,0,1),(15,10,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782978897130_50f0bd55.jpeg','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-003/photo_1782978897130_50f0bd55.jpeg','2026-07-02 07:54:57',NULL,NULL,NULL,NULL,NULL,0,1),(16,10,'DOCUMENT','PENDING','Montfort_Application_APP-U031-2026-0001.pdf','doc_1782978897130_86c418c5.pdf','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-003/doc_1782978897130_86c418c5.pdf','2026-07-02 07:54:57',NULL,NULL,NULL,NULL,NULL,0,1),(17,11,'PHOTO','PENDING','Screenshot24.jpeg','photo_1782979827572_c1d518d2.jpeg','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-004/photo_1782979827572_c1d518d2.jpeg','2026-07-02 08:10:27',NULL,NULL,NULL,NULL,NULL,0,1),(18,11,'DOCUMENT','PENDING','Montfort_Application_APP-U031-2026-0001.pdf','doc_1782979827600_b3d1f4d9.pdf','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-004/doc_1782979827600_b3d1f4d9.pdf','2026-07-02 08:10:27',NULL,NULL,NULL,NULL,NULL,0,1),(19,14,'PHOTO','PENDING','Screenshot24.jpeg','photo_1783005354997_3f41ce74.jpeg','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/photo_1783005354997_3f41ce74.jpeg','2026-07-02 15:15:55',NULL,NULL,NULL,NULL,NULL,0,1),(20,14,'DOCUMENT','PENDING','Montfort_Application_APP-2026-U021-001.pdf','doc_1783005355009_ca91e0bd.pdf','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/doc_1783005355009_ca91e0bd.pdf','2026-07-02 15:15:55',NULL,NULL,NULL,NULL,NULL,0,1),(21,14,'DOCUMENT','PENDING','j.pdf','doc_1783005355011_01b40eef.pdf','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/doc_1783005355011_01b40eef.pdf','2026-07-02 15:15:55',NULL,NULL,NULL,NULL,NULL,0,1),(22,15,'PHOTO','PENDING','Screenshot24.jpeg','photo_1783006785964_dae4189c.jpeg','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-009/photo_1783006785964_dae4189c.jpeg','2026-07-02 15:39:45',NULL,NULL,NULL,NULL,NULL,0,1),(23,15,'DOCUMENT','PENDING','j.pdf','doc_1783006785967_3fe38833.pdf','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-009/doc_1783006785967_3fe38833.pdf','2026-07-02 15:39:45',NULL,NULL,NULL,NULL,NULL,0,1);
/*!40000 ALTER TABLE `erp_application_documents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_application_fees`
--

DROP TABLE IF EXISTS `erp_application_fees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_fees` (
  `fee_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL,
  `base_fee_amount` decimal(12,2) NOT NULL,
  `scholarship_discount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `final_payable` decimal(12,2) NOT NULL,
  `amount_paid` decimal(12,2) NOT NULL DEFAULT 0.00,
  `payment_status` varchar(30) NOT NULL DEFAULT 'PENDING',
  `payment_date` datetime DEFAULT NULL,
  `payment_mode` varchar(30) DEFAULT NULL,
  `receipt_no` varchar(150) DEFAULT NULL,
  `receipt_reference` varchar(150) DEFAULT NULL,
  `collected_by` bigint(20) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`fee_id`),
  UNIQUE KEY `uk_application_fee` (`application_id`),
  UNIQUE KEY `receipt_no` (`receipt_no`),
  KEY `idx_fee_application` (`application_id`),
  KEY `idx_fee_status` (`payment_status`),
  KEY `idx_fee_receipt` (`receipt_no`),
  CONSTRAINT `fk_application_fee` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_application_fees`
--

LOCK TABLES `erp_application_fees` WRITE;
/*!40000 ALTER TABLE `erp_application_fees` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_application_fees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_application_interviews`
--

DROP TABLE IF EXISTS `erp_application_interviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_application_interviews` (
  `interview_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) NOT NULL,
  `teacher_id` bigint(20) NOT NULL,
  `interview_date` datetime DEFAULT NULL,
  `test_score` decimal(5,2) DEFAULT NULL,
  `teacher_remarks` text DEFAULT NULL,
  `recommendation` enum('NOT_RECOMMENDED','RECOMMENDED','WAITLIST') NOT NULL,
  `status` enum('IN_PROGRESS','PENDING','REVIEWED','SUBMITTED') NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`interview_id`),
  UNIQUE KEY `uk_application_interview` (`application_id`),
  KEY `idx_interview_application` (`application_id`),
  KEY `idx_interview_teacher` (`teacher_id`),
  KEY `idx_interview_status` (`status`),
  CONSTRAINT `fk_interview_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_application_interviews`
--

LOCK TABLES `erp_application_interviews` WRITE;
/*!40000 ALTER TABLE `erp_application_interviews` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_application_interviews` ENABLE KEYS */;
UNLOCK TABLES;

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
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`history_id`),
  KEY `fk_application_status_history_application` (`application_id`),
  CONSTRAINT `fk_application_status_history_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_application_status_history`
--

LOCK TABLES `erp_application_status_history` WRITE;
/*!40000 ALTER TABLE `erp_application_status_history` DISABLE KEYS */;
INSERT INTO `erp_application_status_history` VALUES (1,1,NULL,'SUBMITTED',NULL,'2026-07-01 09:44:32','Application submitted by user',1,0),(2,2,NULL,'SUBMITTED',NULL,'2026-07-01 09:54:17','Application submitted by user',1,0),(3,3,NULL,'SUBMITTED',NULL,'2026-07-01 10:02:58','Application submitted by user',1,0),(4,4,NULL,'SUBMITTED',NULL,'2026-07-01 10:07:18','Application submitted by user',1,0),(5,5,NULL,'SUBMITTED',NULL,'2026-07-01 10:32:19','Application submitted by user',1,0),(6,6,NULL,'SUBMITTED',NULL,'2026-07-01 12:00:45','Application submitted by user',1,0),(7,7,NULL,'SUBMITTED',NULL,'2026-07-01 12:24:40','Application submitted by user',1,0),(8,8,NULL,'SUBMITTED',NULL,'2026-07-01 12:37:37','Application submitted by user',1,0),(9,9,NULL,'SUBMITTED',NULL,'2026-07-01 13:24:50','Application submitted by user',1,0),(10,10,NULL,'SUBMITTED',NULL,'2026-07-02 07:54:55','Application submitted by user',1,0),(11,11,NULL,'SUBMITTED',NULL,'2026-07-02 08:10:26','Application submitted by user',1,0),(12,12,NULL,'SUBMITTED',NULL,'2026-07-02 09:24:02','Application submitted by user',1,0),(13,13,NULL,'SUBMITTED',NULL,'2026-07-02 09:24:11','Application submitted by user',1,0),(14,14,NULL,'SUBMITTED',NULL,'2026-07-02 15:15:53','Application submitted by user',1,0),(15,15,NULL,'SUBMITTED',NULL,'2026-07-02 15:39:42','Application submitted by user',1,0),(16,16,NULL,'SUBMITTED',NULL,'2026-07-02 17:28:33','Application submitted by user',1,0),(17,17,NULL,'SUBMITTED',NULL,'2026-07-03 09:58:26','Application submitted by user',1,0),(18,18,NULL,'SUBMITTED',NULL,'2026-07-03 10:27:24','Application submitted by user',1,0),(19,19,NULL,'SUBMITTED',NULL,'2026-07-20 22:13:14','Application submitted by user',1,0);
/*!40000 ALTER TABLE `erp_application_status_history` ENABLE KEYS */;
UNLOCK TABLES;

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
  `term` varchar(20) NOT NULL DEFAULT '',
  `admission_type` varchar(20) DEFAULT 'NEW',
  `student_id` bigint(20) DEFAULT NULL,
  `student_created` tinyint(1) NOT NULL DEFAULT 0,
  `primary_email` varchar(100) DEFAULT NULL,
  `primary_mobile` varchar(20) DEFAULT NULL,
  `first_name` varchar(50) NOT NULL,
  `middle_name` varchar(50) DEFAULT NULL,
  `last_name` varchar(50) NOT NULL,
  `gender` varchar(20) NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(50) DEFAULT 'Uganda',
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
  `photo_path` text DEFAULT NULL,
  `more_info` text DEFAULT NULL,
  `remarks` text DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp(),
  `status` int(11) DEFAULT 1,
  PRIMARY KEY (`application_id`),
  UNIQUE KEY `uk_application_no` (`application_no`),
  KEY `fk_applications_student` (`student_id`),
  CONSTRAINT `fk_applications_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_applications`
--

LOCK TABLES `erp_applications` WRITE;
/*!40000 ALTER TABLE `erp_applications` DISABLE KEYS */;
INSERT INTO `erp_applications` VALUES (1,'APP-2026-U011-001',1,2026,6,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Sibling','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-001/photo_1782899073071_aa106764.jpeg','',NULL,NULL,NULL,'2026-07-01 09:44:32','2026-07-01 09:44:32',1),(2,'APP-2026-U011-002',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Sibling','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-002/photo_1782899658052_258e0834.jpeg','',NULL,NULL,NULL,'2026-07-01 09:54:17','2026-07-01 09:54:17',1),(3,'APP-2026-U031-001',3,2026,12,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-001/photo_1782900179290_a1efd19b.jpeg','',NULL,NULL,NULL,'2026-07-01 10:02:58','2026-07-01 10:02:58',1),(4,'APP-2026-U021-001',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Step-father','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-001/photo_1782900439677_98a61d45.jpeg','',NULL,NULL,NULL,'2026-07-01 10:07:18','2026-07-01 10:07:18',1),(5,'APP-2026-U011-003',1,2026,6,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','2026-07-01','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-003/doc_1782901940666_2579778c.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-003/photo_1782901940649_7964978a.jpeg','',NULL,NULL,NULL,'2026-07-01 10:32:19','2026-07-01 10:32:19',1),(6,'APP-2026-U031-002',3,2026,11,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','FEMALE','2003-07-02','Uganda','2026-07-01','','koo','koo','','kkkk','p',44,'',NULL,'','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-002/doc_1782907247101_64b18ed3.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','d','ho','j','5','5588','SUBMITTED','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-002/photo_1782907247078_26d0e9c4.jpeg','',NULL,NULL,NULL,'2026-07-01 12:00:45','2026-07-01 12:00:45',1),(7,'APP-2026-U011-004',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2003-07-02','Uganda','2026-07-01','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-004/doc_1782908680956_0f8b26f7.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','d','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-004/photo_1782908680940_e24faa98.jpeg','',NULL,NULL,NULL,'2026-07-01 12:24:40','2026-07-01 12:24:40',1),(8,'APP-2026-U011-005',1,2026,6,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2003-07-02','Uganda','2026-07-01','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-005/doc_1782909458395_1f0a7a1f.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-005/photo_1782909458376_86e95df1.jpeg','',NULL,NULL,NULL,'2026-07-01 12:37:37','2026-07-01 12:37:37',1),(9,'APP-2026-U021-002',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2002-01-01','Uganda','2026-07-01','','koo','koo','','','',NULL,'',NULL,'','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-002/doc_1782912293672_bb65484d.pdf;','Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','','','','',0,'central','d','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-002/photo_1782912293668_5a468c1d.jpeg','',NULL,NULL,NULL,'2026-07-01 13:24:50','2026-07-01 13:24:50',1),(10,'APP-2026-U021-003',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-06-30','Uganda','2026-07-02','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-003/doc_1782978897130_86c418c5.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Aunt','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-003/photo_1782978897130_50f0bd55.jpeg','',NULL,NULL,NULL,'2026-07-02 07:54:55','2026-07-02 07:54:55',1),(11,'APP-2026-U021-004',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-06-29','Uganda','2026-07-02','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-004/doc_1782979827600_b3d1f4d9.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Aunt','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-004/photo_1782979827572_c1d518d2.jpeg','',NULL,NULL,NULL,'2026-07-02 08:10:26','2026-07-02 08:10:26',1),(12,'APP-2026-U011-006',1,2026,1,'','NEW',NULL,0,'jai@gmail','+256786844243','jai','','mana','MALE','2024-08-14','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','','','','','','',0,'','','','','',0,'','','','','Grandparent','','','',0,'','Wakisa','','','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-02 09:24:02','2026-07-02 09:24:02',1),(13,'APP-2026-U011-007',1,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2002-01-01','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','','','','',0,'central','gg','ho','j','5','5588','SUBMITTED','','',NULL,NULL,NULL,'2026-07-02 09:24:11','2026-07-02 09:24:11',1),(14,'APP-2026-U011-008',1,2026,3,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 88','Shiva','GANESH','Kurada','MALE','2002-01-01','Uganda','2026-07-02','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/doc_1783005355009_ca91e0bd.pdf;/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/doc_1783005355011_01b40eef.pdf;','Sri Ganesh Sudhagani','+256 6','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Brother','','','',0,'','gg','','Miyapur','','','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/photo_1783005354997_3f41ce74.jpeg','',NULL,NULL,NULL,'2026-07-02 15:15:53','2026-07-02 15:15:53',1),(15,'APP-2026-U011-009',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 123','Shiva','','Kurada','MALE','2022-11-28','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-009/doc_1783006785967_3fe38833.pdf;','Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Grandparent','','','',0,'central','gg','ho','Miyapur','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-009/photo_1783006785964_dae4189c.jpeg','',NULL,NULL,NULL,'2026-07-02 15:39:42','2026-07-02 15:39:42',1),(16,'APP-2026-U011-010',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 79','Sudhagani','','Ganesh','MALE','2022-12-01','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','','Hh','','','','',0,'Nv','','','','',0,'','','','','','','','',0,'','G','','','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-02 17:28:33','2026-07-02 17:28:33',1),(17,'APP-2026-U011-011',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 90','Shiva','GANESH','Kurada','MALE','2002-01-01','Uganda','2026-07-03','','koo','koo','','','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','+256 6','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Brother','','','',0,'','gg','','Miyapur','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-03 09:58:26','2026-07-03 09:58:26',1),(18,'APP-2026-U011-012',1,2026,1,'Term II','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 90','Shiva','GANESH','Kurada','MALE','2002-01-01','Uganda','2026-07-03','','koo','koo','','','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','+256 6','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Aunt','','','',0,'','gg','','Miyapur','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-03 10:27:24','2026-07-03 10:27:24',1),(19,'APP-2026-U02322-001',13,2026,1,'Term I','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 09154763356','SRI','GANESH','SUDHAGANI','MALE','2008-07-18','Uganda','2026-07-21','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Uncle','','','',0,'','d','','','','P.O.Box 202','SUBMITTED','','',NULL,NULL,NULL,'2026-07-20 22:13:14','2026-07-20 22:13:14',1);
/*!40000 ALTER TABLE `erp_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_audit_log`
--

DROP TABLE IF EXISTS `erp_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_audit_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `action` varchar(255) NOT NULL,
  `entity` varchar(255) NOT NULL,
  `entity_id` bigint(20) DEFAULT NULL,
  `audit_timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  `details` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_erp_audit_user` (`user_id`),
  KEY `idx_erp_audit_branch_user` (`branch_id`,`user_id`),
  KEY `idx_erp_audit_timestamp` (`audit_timestamp`),
  KEY `idx_erp_audit_entity` (`entity`),
  KEY `idx_erp_audit_action` (`action`),
  CONSTRAINT `fk_erp_audit_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`),
  CONSTRAINT `fk_erp_audit_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_audit_log`
--

LOCK TABLES `erp_audit_log` WRITE;
/*!40000 ALTER TABLE `erp_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `erp_branch_fund_allocations`
--

LOCK TABLES `erp_branch_fund_allocations` WRITE;
/*!40000 ALTER TABLE `erp_branch_fund_allocations` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_branch_fund_allocations` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_branch_levels`
--

LOCK TABLES `erp_branch_levels` WRITE;
/*!40000 ALTER TABLE `erp_branch_levels` DISABLE KEYS */;
INSERT INTO `erp_branch_levels` VALUES (1,1,1,'2026-06-29 14:35:26',NULL),(2,2,1,'2026-06-29 14:35:26',NULL),(5,2,2,'2026-06-29 14:35:26',NULL),(7,3,3,'2026-06-29 14:35:27',NULL),(9,3,4,'2026-06-29 23:26:48','SUPER_ADMIN'),(10,1,2,'2026-06-30 01:25:57','SUPER_ADMIN'),(11,4,3,'2026-07-09 18:56:36','SUPER_ADMIN'),(12,4,4,'2026-07-09 18:56:36','SUPER_ADMIN'),(13,5,2,'2026-07-09 19:04:21','SUPER_ADMIN'),(14,7,3,'2026-07-09 19:10:25','SUPER_ADMIN'),(15,7,4,'2026-07-09 19:10:25','SUPER_ADMIN'),(16,8,2,'2026-07-09 19:21:32','SUPER_ADMIN'),(17,9,2,'2026-07-10 13:03:58','SUPER_ADMIN'),(18,9,3,'2026-07-10 13:03:58','SUPER_ADMIN'),(19,10,1,'2026-07-21 03:38:45','SUPER_ADMIN'),(20,10,2,'2026-07-21 03:38:45','SUPER_ADMIN'),(21,11,1,'2026-07-21 04:00:35','SUPER_ADMIN'),(22,12,2,'2026-07-21 04:14:59','SUPER_ADMIN'),(23,13,1,'2026-07-21 04:33:39','SUPER_ADMIN');
/*!40000 ALTER TABLE `erp_branch_levels` ENABLE KEYS */;
UNLOCK TABLES;

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
  `address_line_1` varchar(255) DEFAULT NULL,
  `address_line_2` varchar(255) DEFAULT NULL,
  `po_box` varchar(100) DEFAULT NULL,
  `locality` varchar(150) DEFAULT NULL,
  `city` varchar(150) DEFAULT NULL,
  `district` varchar(150) DEFAULT NULL,
  `region` varchar(150) DEFAULT NULL,
  `country` varchar(100) NOT NULL DEFAULT 'Uganda',
  `postal_code` varchar(30) DEFAULT NULL,
  `is_active` int(11) DEFAULT 1,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `contact_details` text DEFAULT NULL,
  `primary_phone` varchar(30) DEFAULT NULL,
  `secondary_phone` varchar(30) DEFAULT NULL,
  `whatsapp_phone` enum('NONE','PRIMARY','SECONDARY','BOTH') NOT NULL DEFAULT 'NONE',
  `branch_email` varchar(150) DEFAULT NULL,
  `email_from_name` varchar(150) DEFAULT NULL,
  `email_reply_to` varchar(150) DEFAULT NULL,
  `email_enabled` tinyint(1) NOT NULL DEFAULT 1,
  `foundation_date` varchar(255) DEFAULT NULL,
  `gov_document_url` varchar(255) DEFAULT NULL,
  `incharge_details` text DEFAULT NULL,
  `school_photo_url` varchar(255) DEFAULT NULL,
  `branch_logo_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_branches`
--

LOCK TABLES `erp_branches` WRITE;
/*!40000 ALTER TABLE `erp_branches` DISABLE KEYS */;
INSERT INTO `erp_branches` VALUES (1,'St. Kizito Nursery & Primary School','U011','Kyebando',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-06-25 08:43:09.000000',NULL,'2026-06-25 08:43:09.000000',NULL,'-',NULL,NULL,'NONE',NULL,NULL,NULL,1,'2024-05-25',NULL,'[]',NULL,NULL),(2,'St.Montfort Nursery & Primary School','U021','Mpala',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-06-25 08:45:46.000000',NULL,'2026-06-25 08:45:46.000000',NULL,'-',NULL,NULL,'NONE',NULL,NULL,NULL,1,'2022-04-25',NULL,'[]',NULL,NULL),(3,'Pere Achte Secondary & Senior Secondary School','U031','Isunga',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-06-25 08:46:45.000000',NULL,'2026-06-25 08:46:45.000000',NULL,'-',NULL,NULL,'NONE',NULL,NULL,NULL,1,'2022-04-25',NULL,'[]',NULL,NULL),(4,'St.Montfort Nursery & Primary School','U032','Isunga',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-07-09 11:55:06.000000','1','2026-07-09 11:55:06.000000','1','22348888988',NULL,NULL,'NONE',NULL,NULL,NULL,1,NULL,NULL,'[]',NULL,NULL),(5,'St.Montfort Nursery & Primary School','U032','Isunga',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-07-09 11:56:54.000000','1','2026-07-09 11:56:54.000000','1','22348888988',NULL,NULL,'NONE',NULL,NULL,NULL,1,NULL,NULL,'[]',NULL,NULL),(6,'St.Montfort Nursery & Primary School','U032','Isunga',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-07-09 12:02:55.000000','1','2026-07-09 12:02:55.000000','1','22348888988',NULL,NULL,'NONE',NULL,NULL,NULL,1,NULL,NULL,'[]',NULL,NULL),(7,'St.Montfort Nursery & Primary School','U032','Isunga',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-07-09 12:10:25.000000','1','2026-07-09 12:19:58.000000','1','22348888988',NULL,NULL,'NONE',NULL,NULL,NULL,1,NULL,NULL,'[]',NULL,NULL),(8,'St.Montfort Nursery & Primary School','U033','Isunga',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-07-09 12:21:32.000000','1','2026-07-09 12:21:32.000000','1','22348888988',NULL,NULL,'NONE',NULL,NULL,NULL,1,NULL,NULL,'[{\"name\":\"f\",\"role\":\"ff\",\"phone\":\"6888\"}]',NULL,NULL),(9,'St.Montfort Nursery & Primary School','U034','Isunga',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Uganda',NULL,1,'2026-07-10 06:03:58.000000','1','2026-07-11 08:07:08.000000','1','22348888988',NULL,NULL,'NONE',NULL,NULL,NULL,1,NULL,NULL,'[{\"name\":\"Hello\",\"role\":\"hello\",\"phone\":\"789456122\"}]','/uploads/branchdetails/U034-St.Montfort_Nursery___Primary_School-Isunga/photo/1783668745418_Screenshot24.jpeg',NULL),(10,'St.Montfort Nursery & Primary School','U036','Mpala','mpla','mpala','P.O.Box 202','','','','','Uganda','',1,'2026-07-20 20:38:45.000000','1','2026-07-20 20:38:45.000000','1','Primary: 09154763356 | WhatsApp: None | Email: sriganeshgoud9154@gmail.com','09154763356','','NONE','sriganeshgoud9154@gmail.com','St.Montfort Nursery & Primary School','sriganeshgoud9154@gmail.com',1,'2024-09-11','/uploads/branchdetails/U036-St.Montfort_Nursery___Primary_School-Mpala/documents/1784579925524_Montfort_Application_APP-U031-2026-0001.pdf','[{\"name\":\"Sri Ganesh\",\"role\":\"S\",\"phone\":\"09154763356\"}]','/uploads/branchdetails/U036-St.Montfort_Nursery___Primary_School-Mpala/photo/1784579925520_Screenshot24.jpeg','/uploads/branchdetails/U036-St.Montfort_Nursery___Primary_School-Mpala/logo/1784579925505_Screenshot__772_.png'),(11,'St.Montfort Nursery & Primary School','U202','Mpala','mpla','mpala','P.O.Box 202','','','','','Uganda','',1,'2026-07-20 21:00:35.000000','1','2026-07-20 21:00:36.000000','1','Primary: 0915476338 | WhatsApp: Phone Number 1 | Email: sriganeshgoud@gmail.com','0915476338','','PRIMARY','sriganeshgoud@gmail.com','St.Montfort Nursery & Primary School','sriganeshgoud@gmail.com',1,'2024-09-11','branches/11-U202-St-Montfort-Nursery-Primary-School-Mpala/documents/government-document-450c6455-cc71-41c4-99de-2ea4195b317e.pdf','[{\"name\":\"Sri Ganesh\",\"role\":\"S\",\"phone\":\"09154763356\"}]','branches/11-U202-St-Montfort-Nursery-Primary-School-Mpala/photo/school-photo-bb28d9a2-9513-459c-8b65-a7d5568e1671.jpeg','branches/11-U202-St-Montfort-Nursery-Primary-School-Mpala/logo/branch-logo-60ba2982-1835-414e-8f43-acf167bfcc06.png'),(12,'St.Montfort Nursery & Primary School','U525','Mpala','mpla','mpala','P.O.Box 202','','','','','Uganda','',1,'2026-07-20 21:14:59.000000','1','2026-07-20 21:14:59.000000','1','Primary: 09154763389 | WhatsApp: None | Email: 201020468002@lfdc.edu.in','09154763389','','NONE','201020468002@lfdc.edu.in','St.Montfort Nursery & Primary School','201020468002@lfdc.edu.in',1,'2024-09-11','branches/12-U525-St-Montfort-Nursery-Primary-School-Mpala/documents/government-document-743101e1-7e76-401f-af6a-75d9dae55da8.pdf','[{\"name\":\"Sri Ganesh\",\"role\":\"S\",\"phone\":\"09154763356\"}]','branches/12-U525-St-Montfort-Nursery-Primary-School-Mpala/photo/school-photo-cf65f535-a264-4472-b73d-c34bee12ea4e.jpeg','branches/12-U525-St-Montfort-Nursery-Primary-School-Mpala/logo/branch-logo-16f66ff3-1067-403c-8874-6e22315380d7.png'),(13,'St.Montfort Nursery & Primary School','U02322','Mpala','mpla','mpala','P.O.Box 202','','','','','Uganda','',1,'2026-07-20 21:33:39.000000','1','2026-07-20 21:33:40.000000','1','Primary: 09154763389 | WhatsApp: None | Email: sudhaganisriganesh@gmail.com','09154763389','','NONE','sudhaganisriganesh@gmail.com','St.Montfort Nursery & Primary School','sudhaganisriganesh@gmail.com',1,'2024-09-11',NULL,'[{\"name\":\"Sri Ganesh\",\"role\":\"S\",\"phone\":\"09154763356\"}]','branches/13-U02322-St-Montfort-Nursery-Primary-School-Mpala/photo/school-photo-226215a9-9d37-4bf5-93dc-ef97005c6236.jpeg','branches/13-U02322-St-Montfort-Nursery-Primary-School-Mpala/logo/branch-logo-4bbf66cf-aded-4e97-886c-7bbdeb7c0abc.png');
/*!40000 ALTER TABLE `erp_branches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_classes`
--

DROP TABLE IF EXISTS `erp_classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_classes` (
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

--
-- Dumping data for table `erp_classes`
--

LOCK TABLES `erp_classes` WRITE;
/*!40000 ALTER TABLE `erp_classes` DISABLE KEYS */;
INSERT INTO `erp_classes` VALUES (1,1,'N1','Baby Class',1,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(2,1,'N2','Middle Class',2,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(3,1,'N3','Top Class',3,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(4,2,'P1','Primary 1',4,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(5,2,'P2','Primary 2',5,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(6,2,'P3','Primary 3',6,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(7,2,'P4','Primary 4',7,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(8,2,'P5','Primary 5',8,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(9,2,'P6','Primary 6',9,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(10,2,'P7','Primary 7',10,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(11,3,'S1','Secondary 1',11,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(12,3,'S2','Secondary 2',12,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(13,3,'S3','Secondary 3',13,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(14,3,'S4','Secondary 4',14,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(15,4,'S5','Senior Secondary 5',15,1,'2026-06-29 14:52:00','2026-06-29 14:52:00'),(16,4,'S6','Senior Secondary 6',16,1,'2026-06-29 14:52:00','2026-06-29 14:52:00');
/*!40000 ALTER TABLE `erp_classes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_departments`
--

DROP TABLE IF EXISTS `erp_departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_departments` (
  `department_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` int(11) NOT NULL,
  `department_code` varchar(20) NOT NULL,
  `department_name` varchar(100) NOT NULL,
  `is_academic` tinyint(1) NOT NULL DEFAULT 1,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`department_id`),
  UNIQUE KEY `uk_branch_dept_code` (`branch_id`,`department_code`),
  UNIQUE KEY `uk_branch_dept_name` (`branch_id`,`department_name`),
  CONSTRAINT `fk_department_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_departments`
--

LOCK TABLES `erp_departments` WRITE;
/*!40000 ALTER TABLE `erp_departments` DISABLE KEYS */;
INSERT INTO `erp_departments` VALUES (1,1,'ACA','Academics',1,'General teaching and academic activities','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(2,1,'EXAM','Examinations',1,'Examinations, grading, and assessments','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(3,1,'LIB','Library',1,'School library and resources management','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(4,1,'DISC','Discipline',1,'Student discipline and welfare','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(5,1,'GNC','Guidance & Counseling',1,'Student mental health and career guidance','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(6,1,'MATH','Mathematics',1,'Mathematics Department','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(7,1,'ENG','English',1,'English Language and Literature','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(8,1,'SCI','Science',1,'Physics, Chemistry, and Biology','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(9,1,'SST','Social Studies',1,'History, Geography, and Social Studies','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(10,1,'LANG','Languages',1,'Local and Foreign Languages (e.g. Swahili, French)','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(11,1,'COMP','Computer Studies',1,'Computer Science and IT Academics','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(12,1,'ARTS','Arts & Crafts',1,'Fine Art, Music, Dance, and Drama','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(13,1,'PET','Physical Education & Sports',1,'Sports and Co-curricular activities','ACTIVE',1,1,'1','2026-07-14 09:24:37','1','2026-07-14 16:44:44'),(14,1,'REL','Religious Education',1,'CRE, IRE, and Moral Education','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(15,1,'VOC','Vocational Studies',1,'Agriculture, Home Economics, Woodwork, etc.','ACTIVE',1,0,'1','2026-07-14 09:24:37','1','2026-07-14 09:24:37'),(16,1,'ADMN','Administration',0,'School administration and management','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(17,1,'ADMS','Admissions',0,'Admissions and student enrollment','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(18,1,'FIN','Finance',0,'Accounts and school finance','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(19,1,'HR','Human Resources',0,'Human resource and staff management','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(20,1,'ICT','ICT Support',0,'IT infrastructure and technical support','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(21,1,'HLTH','Health Unit',0,'School clinic and medical services','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(22,1,'TRAN','Transport',0,'School transport and fleet management','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(23,1,'HSTL','Hostel',0,'Boarding and hostel management','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(24,1,'PROC','Procurement',0,'Purchasing and school procurement','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(25,1,'MNT','Maintenance',0,'Maintenance of school facilities','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(26,1,'SEC','Security',0,'School security and guarding','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(27,1,'EST','Estate',0,'Estate and infrastructure management','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(28,1,'STR','Store',0,'Stores and inventory management','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(29,1,'PR','Public Relations',0,'Public relations and communication','ACTIVE',1,0,'1','2026-07-14 09:24:38','1','2026-07-14 09:24:38'),(30,2,'SCI','Science',1,'','ACTIVE',1,0,'3','2026-07-16 09:37:24','3','2026-07-16 09:37:24'),(32,1,'SCIE','Sciencesddd',1,'','ACTIVE',1,0,'2','2026-07-16 18:05:34','2','2026-07-16 18:05:34'),(33,2,'math','maths',1,'','ACTIVE',1,0,'3','2026-07-18 06:11:05','3','2026-07-18 06:11:05'),(35,2,'PR','Public Relation',1,'','ACTIVE',1,0,'3','2026-07-18 06:29:04','3','2026-07-18 06:29:04'),(36,2,'STR','Store',0,'','ACTIVE',1,0,'3','2026-07-18 06:30:11','3','2026-07-18 06:30:11'),(37,2,'STRS','stores',0,'','ACTIVE',1,0,'3','2026-07-18 07:08:06','3','2026-07-18 07:08:06'),(38,3,'STR','store',1,'','ACTIVE',1,0,'4','2026-07-18 07:50:24','4','2026-07-18 07:50:24'),(39,1,'SCSAC','scsdcsdc',1,'','ACTIVE',1,0,'2','2026-07-18 14:20:16','2','2026-07-18 14:20:16'),(40,13,'SCI','mathss',1,'','ACTIVE',1,1,'26','2026-07-20 21:58:52','26','2026-07-20 21:59:04');
/*!40000 ALTER TABLE `erp_departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_designations`
--

DROP TABLE IF EXISTS `erp_designations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_designations` (
  `designation_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `designation_code` varchar(20) NOT NULL,
  `designation_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`designation_id`),
  UNIQUE KEY `uk_designation_code` (`designation_code`),
  UNIQUE KEY `uk_designation_name` (`designation_name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_designations`
--

LOCK TABLES `erp_designations` WRITE;
/*!40000 ALTER TABLE `erp_designations` DISABLE KEYS */;
INSERT INTO `erp_designations` VALUES (1,'DIRECTOR','Director','School Director','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(2,'PRINCIPAL','Principal','Head of the school','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(3,'VICE_PRINCIPAL','Vice Principal','Assists the Principal','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(4,'HEAD_TEACHER','Head Teacher','Head of academic activities','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(5,'DOS','Dean of Studies (DOS)','Dean of Studies','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(6,'HOD','HOD','Head of Department','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(7,'TEACHER','Teacher','Teaching staff','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(8,'BURSAR','Bursar','School Bursar','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(9,'ADMISSIONS_OFFICER','Admissions Officer','Handles admissions and enrollment','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(10,'ACCOUNTANT','Accountants','Manages finance and accounts','ACTIVE',1,1,NULL,'2026-07-14 07:27:59','26','2026-07-20 21:59:47'),(11,'LIBRARIAN','Librarian','Manages library resources','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(12,'ICT_OFFICER','ICT Officer','Provides IT support','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(13,'SECRETARY','Secretary','Administrative secretary','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(14,'RECEPTIONIST','Receptionist','Front office and visitor management','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(15,'SCHOOL_NURSE','School Nurse','Provides medical assistance','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(16,'COUNSELOR','Counselor','Student guidance and counseling','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(17,'DRIVER','Driver','School transport driver','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(18,'SECURITY_GUARD','Security Guard','Campus security','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(19,'CLEANER','Cleaner','Cleaning and housekeeping','ACTIVE',1,0,NULL,'2026-07-14 07:27:59',NULL,'2026-07-14 07:27:59'),(20,'MATH','Mathematic','','ACTIVE',1,0,'3','2026-07-16 09:36:31','3','2026-07-16 09:36:31'),(21,'MATHassdf','Mathematiceffe','','ACTIVE',1,0,'2','2026-07-16 18:10:17','2','2026-07-16 18:10:17'),(22,'MATHSS','MathematicAA','','ACTIVE',1,0,'3','2026-07-18 06:14:03','3','2026-07-18 06:14:03'),(23,'casda','cxzxcz','','ACTIVE',1,0,'2','2026-07-18 14:20:10','2','2026-07-18 14:20:10'),(24,'hskadka','knkscbksdcbk','','ACTIVE',1,0,'26','2026-07-20 22:05:09','26','2026-07-20 22:05:09');
/*!40000 ALTER TABLE `erp_designations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_document_sequences`
--

DROP TABLE IF EXISTS `erp_document_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_document_sequences` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `module_code` varchar(255) NOT NULL,
  `running_year` int(11) NOT NULL,
  `current_sequence` bigint(20) NOT NULL DEFAULT 0,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `active` bit(1) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_seq` (`branch_id`,`module_code`,`running_year`),
  UNIQUE KEY `UKhhwyasi3o241inhufw6ki7k1l` (`branch_id`,`module_code`,`running_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_document_sequences`
--

LOCK TABLES `erp_document_sequences` WRITE;
/*!40000 ALTER TABLE `erp_document_sequences` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_document_sequences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_employee_contacts`
--

DROP TABLE IF EXISTS `erp_employee_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_contacts` (
  `employee_contact_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `employee_contact_name` varchar(255) NOT NULL,
  `employee_contact_relationship` enum('BROTHER','DAUGHTER','FATHER','FRIEND','GUARDIAN','MANAGER','MOTHER','OTHER','REFERENCE','RELATIVE','SISTER','SON','SPOUSE') NOT NULL,
  `employee_contact_type` enum('EMERGENCY','GUARDIAN','NEXT_OF_KIN','OTHER','REFERENCE') NOT NULL,
  `employee_contact_mobile` varchar(30) NOT NULL,
  `employee_contact_alternate_mobile` varchar(30) DEFAULT NULL,
  `employee_contact_email` varchar(150) DEFAULT NULL,
  `employee_contact_country` varchar(100) DEFAULT NULL,
  `employee_contact_state` varchar(100) DEFAULT NULL,
  `employee_contact_district` varchar(100) DEFAULT NULL,
  `employee_contact_village` varchar(150) DEFAULT NULL,
  `employee_contact_street` varchar(255) DEFAULT NULL,
  `employee_contact_postal_code` varchar(30) DEFAULT NULL,
  `employee_contact_occupation` varchar(150) DEFAULT NULL,
  `employee_contact_workplace` varchar(255) DEFAULT NULL,
  `employee_contact_is_primary` tinyint(1) NOT NULL DEFAULT 0,
  `employee_contact_is_emergency` tinyint(1) NOT NULL DEFAULT 1,
  `employee_contact_active` tinyint(1) NOT NULL DEFAULT 1,
  `employee_contact_remarks` text DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_contact_id`),
  KEY `fk_empcontact_employee` (`employee_id`),
  KEY `fk_empcontact_createdby` (`created_by`),
  KEY `fk_empcontact_updatedby` (`updated_by`),
  CONSTRAINT `fk_empcontact_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_contacts`
--

LOCK TABLES `erp_employee_contacts` WRITE;
/*!40000 ALTER TABLE `erp_employee_contacts` DISABLE KEYS */;
INSERT INTO `erp_employee_contacts` VALUES (2,7,'dsds','RELATIVE','EMERGENCY','9652646446',NULL,'prabhakar@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,1,1,NULL,'2','2026-07-18 18:36:55','2','2026-07-18 18:36:55',0);
/*!40000 ALTER TABLE `erp_employee_contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_employee_documents`
--

DROP TABLE IF EXISTS `erp_employee_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_documents` (
  `employee_document_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `employee_document_type` varchar(100) NOT NULL,
  `employee_document_name` varchar(255) NOT NULL,
  `employee_document_description` text DEFAULT NULL,
  `employee_document_file_name` varchar(255) NOT NULL,
  `employee_document_original_file_name` varchar(255) DEFAULT NULL,
  `employee_document_file_path` varchar(500) NOT NULL,
  `employee_document_file_extension` varchar(20) DEFAULT NULL,
  `employee_document_mime_type` varchar(100) DEFAULT NULL,
  `employee_document_file_size` bigint(20) DEFAULT NULL,
  `employee_document_issue_date` date DEFAULT NULL,
  `employee_document_expiry_date` date DEFAULT NULL,
  `employee_document_verified` tinyint(1) NOT NULL DEFAULT 0,
  `employee_document_verified_by` int(11) DEFAULT NULL,
  `employee_document_verified_at` datetime DEFAULT NULL,
  `employee_document_is_mandatory` tinyint(1) NOT NULL DEFAULT 0,
  `employee_document_active` tinyint(1) NOT NULL DEFAULT 1,
  `employee_document_remarks` text DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_document_id`),
  KEY `fk_empdocument_employee` (`employee_id`),
  KEY `fk_empdocument_verifiedby` (`employee_document_verified_by`),
  CONSTRAINT `fk_empdocument_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empdocument_verifiedby` FOREIGN KEY (`employee_document_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_documents`
--

LOCK TABLES `erp_employee_documents` WRITE;
/*!40000 ALTER TABLE `erp_employee_documents` DISABLE KEYS */;
INSERT INTO `erp_employee_documents` VALUES (1,8,'SALARY_CERTIFICATE','Nationa','204dkii','SALARY_CERTIFICATE_Montfort_Application_APP-2026-U021-001.pdf','Montfort_Application_APP-2026-U021-001.pdf','U011-St. Kizito Nursery _ Primary School,Kyebando/staff/U011-T-26-004-bhanu bhanyu/other_1784400061042_ea4c2c.pdf','pdf','application/pdf',4093,NULL,NULL,0,NULL,NULL,0,1,NULL,'2','2026-07-18 18:41:01','2','2026-07-18 18:41:01',0),(3,39,'RELIEVING_LETTER','He','7895','RELIEVING_LETTER_Receipt_U011-26-005__1_.pdf','Receipt_U011-26-005 (1).pdf','U011-St. Kizito Nursery _ Primary School,Kyebando/staff/U011-T-26-010-Sri Ganesh Sudhagani/other_1784477241590_ed37fea1.pdf','pdf','application/pdf',338369,NULL,NULL,0,NULL,NULL,0,1,NULL,'2','2026-07-19 16:07:21','2','2026-07-19 16:07:21',0);
/*!40000 ALTER TABLE `erp_employee_documents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_employee_experience`
--

DROP TABLE IF EXISTS `erp_employee_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_experience` (
  `employee_experience_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `employee_experience_type` enum('FULL_TIME','PART_TIME','CONTRACT','TEMPORARY','INTERNSHIP','CONSULTANT','VOLUNTEER','SELF_EMPLOYED','OTHER') DEFAULT 'FULL_TIME',
  `employee_experience_company_name` varchar(255) NOT NULL,
  `employee_experience_company_address` varchar(255) DEFAULT NULL,
  `employee_experience_company_country` varchar(100) DEFAULT NULL,
  `employee_experience_company_state` varchar(100) DEFAULT NULL,
  `employee_experience_company_district` varchar(100) DEFAULT NULL,
  `employee_experience_designation` varchar(150) DEFAULT NULL,
  `employee_experience_department` varchar(150) DEFAULT NULL,
  `employee_experience_employment_type` enum('FULL_TIME','PART_TIME','CONTRACT','TEMPORARY','INTERNSHIP','CONSULTANT','VOLUNTEER','SELF_EMPLOYED','OTHER') NOT NULL,
  `employee_experience_start_date` date NOT NULL,
  `employee_experience_end_date` date DEFAULT NULL,
  `employee_experience_current_job` tinyint(1) NOT NULL DEFAULT 0,
  `employee_experience_total_months` int(11) DEFAULT NULL,
  `employee_experience_salary` decimal(15,2) DEFAULT NULL,
  `employee_experience_currency` varchar(10) DEFAULT NULL,
  `employee_experience_supervisor_name` varchar(255) DEFAULT NULL,
  `employee_experience_supervisor_contact` varchar(100) DEFAULT NULL,
  `employee_experience_reason_for_leaving` varchar(255) DEFAULT NULL,
  `employee_experience_responsibilities` text DEFAULT NULL,
  `employee_experience_achievements` text DEFAULT NULL,
  `employee_experience_experience_certificate_file` varchar(500) DEFAULT NULL,
  `employee_experience_relieving_letter_file` varchar(500) DEFAULT NULL,
  `employee_experience_verified` tinyint(1) NOT NULL DEFAULT 0,
  `employee_experience_verified_by` int(11) DEFAULT NULL,
  `employee_experience_verified_at` datetime DEFAULT NULL,
  `employee_experience_active` tinyint(1) NOT NULL DEFAULT 1,
  `employee_experience_remarks` text DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_experience_id`),
  KEY `fk_empexperience_employee` (`employee_id`),
  KEY `fk_empexperience_verifiedby` (`employee_experience_verified_by`),
  CONSTRAINT `fk_empexperience_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empexperience_verifiedby` FOREIGN KEY (`employee_experience_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_experience`
--

LOCK TABLES `erp_employee_experience` WRITE;
/*!40000 ALTER TABLE `erp_employee_experience` DISABLE KEYS */;
INSERT INTO `erp_employee_experience` VALUES (1,7,'FULL_TIME','fdsgd',NULL,NULL,NULL,NULL,'dfvsd',NULL,'INTERNSHIP','2026-02-10','2026-07-05',0,5,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,1,NULL,'2','2026-07-18 18:36:55','2','2026-07-18 18:36:55',0);
/*!40000 ALTER TABLE `erp_employee_experience` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_employee_qualifications`
--

DROP TABLE IF EXISTS `erp_employee_qualifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_qualifications` (
  `employee_qualification_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `employee_qualification_level` enum('PRIMARY','SECONDARY','SENIOR_SECONDARY','DIPLOMA','CERTIFICATE','GRADUATION','POST_GRADUATION','DR_PHD','OTHER') NOT NULL,
  `custom_level` varchar(255) DEFAULT NULL,
  `employee_qualification_name` varchar(255) NOT NULL,
  `employee_qualification_specialization` varchar(255) DEFAULT NULL,
  `employee_qualification_institution_name` varchar(255) NOT NULL,
  `qualification_grade` varchar(100) DEFAULT NULL,
  `employee_qualification_board_university` varchar(255) DEFAULT NULL,
  `employee_qualification_country` varchar(100) DEFAULT NULL,
  `employee_qualification_start_year` int(11) DEFAULT NULL,
  `employee_qualification_completion_year` int(11) DEFAULT NULL,
  `employee_qualification_duration_months` int(11) DEFAULT NULL,
  `employee_qualification_grade` varchar(50) DEFAULT NULL,
  `employee_qualification_percentage` decimal(5,2) DEFAULT NULL,
  `employee_qualification_cgpa` decimal(4,2) DEFAULT NULL,
  `employee_qualification_certificate_number` varchar(100) DEFAULT NULL,
  `employee_qualification_registration_number` varchar(100) DEFAULT NULL,
  `employee_qualification_document_file` varchar(500) DEFAULT NULL,
  `employee_qualification_verified` tinyint(1) NOT NULL DEFAULT 0,
  `employee_qualification_verified_by` int(11) DEFAULT NULL,
  `employee_qualification_verified_at` datetime DEFAULT NULL,
  `employee_qualification_remarks` text DEFAULT NULL,
  `employee_qualification_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_qualification_id`),
  KEY `fk_empqualification_employee` (`employee_id`),
  KEY `fk_empqualification_verifiedby` (`employee_qualification_verified_by`),
  CONSTRAINT `fk_empqualification_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empqualification_verifiedby` FOREIGN KEY (`employee_qualification_verified_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_qualifications`
--

LOCK TABLES `erp_employee_qualifications` WRITE;
/*!40000 ALTER TABLE `erp_employee_qualifications` DISABLE KEYS */;
INSERT INTO `erp_employee_qualifications` VALUES (2,5,'PRIMARY',NULL,'lug',NULL,'lfs',NULL,NULL,NULL,NULL,2021,NULL,'A',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,'2','2026-07-18 18:32:47','2','2026-07-18 18:32:47',0),(4,7,'SECONDARY',NULL,'SS',NULL,'AAS',NULL,NULL,NULL,NULL,2022,NULL,'A',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,'2','2026-07-18 18:36:55','2','2026-07-18 18:36:55',0),(5,7,'SENIOR_SECONDARY',NULL,'SDD',',knkabKb','kjkk',NULL,NULL,NULL,NULL,2025,NULL,'A',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,NULL,1,'2','2026-07-18 18:36:55','2','2026-07-18 18:36:55',0),(7,17,'PRIMARY',NULL,'dasd',NULL,'sdas',NULL,NULL,NULL,NULL,2022,NULL,'asda',NULL,NULL,NULL,NULL,NULL,0,NULL,NULL,'[File: U011-St. Kizito Nursery _ Primary School,Kyebando/staff/U011-T-26-006-Sri Ganesh Sudhagani/certificate_1784402005075_543fe0a6.pdf]',1,'2','2026-07-18 19:13:24','2','2026-07-18 19:13:29',1);
/*!40000 ALTER TABLE `erp_employee_qualifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_employee_sequences`
--

DROP TABLE IF EXISTS `erp_employee_sequences`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_sequences` (
  `sequence_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` int(11) NOT NULL,
  `employee_category` varchar(50) NOT NULL,
  `sequence_year` int(11) NOT NULL,
  `last_number` int(11) NOT NULL DEFAULT 0,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`sequence_id`),
  UNIQUE KEY `uk_employee_sequence` (`branch_id`,`employee_category`,`sequence_year`),
  CONSTRAINT `fk_employee_sequence_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_sequences`
--

LOCK TABLES `erp_employee_sequences` WRITE;
/*!40000 ALTER TABLE `erp_employee_sequences` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_employee_sequences` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_employees`
--

DROP TABLE IF EXISTS `erp_employees`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employees` (
  `employee_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `branch_id` int(11) NOT NULL,
  `department_id` bigint(20) DEFAULT NULL,
  `designation_id` bigint(20) DEFAULT NULL,
  `reporting_manager_id` bigint(20) DEFAULT NULL,
  `employee_no` varchar(50) NOT NULL,
  `title` varchar(20) DEFAULT NULL,
  `first_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) NOT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` enum('FEMALE','MALE','OTHER') DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `profile_photo` varchar(500) DEFAULT NULL,
  `signature_file` varchar(500) DEFAULT NULL,
  `nationality` varchar(100) DEFAULT NULL,
  `national_id` varchar(100) DEFAULT NULL,
  `passport_no` varchar(100) DEFAULT NULL,
  `tin_number` varchar(100) DEFAULT NULL,
  `marital_status` varchar(50) DEFAULT NULL,
  `blood_group` varchar(20) DEFAULT NULL,
  `religion` varchar(100) DEFAULT NULL,
  `sub_religion` varchar(100) DEFAULT NULL,
  `official_email` varchar(150) DEFAULT NULL,
  `personal_email` varchar(150) DEFAULT NULL,
  `mobile_no` varchar(30) DEFAULT NULL,
  `alternate_mobile` varchar(30) DEFAULT NULL,
  `address_country` varchar(100) DEFAULT NULL,
  `address_state` varchar(100) DEFAULT NULL,
  `address_district` varchar(100) DEFAULT NULL,
  `address_county` varchar(100) DEFAULT NULL,
  `address_sub_county` varchar(100) DEFAULT NULL,
  `address_parish` varchar(100) DEFAULT NULL,
  `address_village` varchar(150) DEFAULT NULL,
  `address_street` varchar(255) DEFAULT NULL,
  `postal_code` varchar(30) DEFAULT NULL,
  `employee_category` enum('TEACHING','NON_TEACHING','MANAGEMENT_TEACHING','MANAGEMENT_NON_TEACHING','SUPPORT_STAFF') DEFAULT NULL,
  `employee_type` enum('PERMANENT','CONTRACT','TEMPORARY','PART_TIME','INTERN','VOLUNTEER','HONORY') NOT NULL,
  `employment_mode` enum('FULL_TIME','ON_CALL','PART_TIME','REMOTE') NOT NULL,
  `employment_status` enum('ACTIVE','ON_LEAVE','PROBATION','RESIGNED','RETIRED','SUSPENDED','TERMINATED') NOT NULL,
  `joining_date` date DEFAULT NULL,
  `probation_end_date` date DEFAULT NULL,
  `confirmation_date` date DEFAULT NULL,
  `retirement_date` date DEFAULT NULL,
  `resignation_date` date DEFAULT NULL,
  `termination_date` date DEFAULT NULL,
  `work_permit_number` varchar(100) DEFAULT NULL,
  `work_permit_expiry_date` date DEFAULT NULL,
  `passport_expiry_date` date DEFAULT NULL,
  `employment_end_date` date DEFAULT NULL,
  `exit_reason` text DEFAULT NULL,
  `skills` text DEFAULT NULL,
  `languages_spoken` text DEFAULT NULL,
  `employee_remarks` text DEFAULT NULL,
  `login_enabled` tinyint(1) NOT NULL DEFAULT 0,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `employee_no` (`employee_no`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `fk_employee_reporting_manager` (`reporting_manager_id`),
  KEY `idx_emp_branch` (`branch_id`),
  KEY `idx_emp_department` (`department_id`),
  KEY `idx_emp_designation` (`designation_id`),
  KEY `idx_emp_status` (`employment_status`),
  KEY `idx_emp_category` (`employee_category`),
  CONSTRAINT `fk_employee_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_department` FOREIGN KEY (`department_id`) REFERENCES `erp_departments` (`department_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_designation` FOREIGN KEY (`designation_id`) REFERENCES `erp_designations` (`designation_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_reporting_manager` FOREIGN KEY (`reporting_manager_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_employee_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employees`
--

LOCK TABLES `erp_employees` WRITE;
/*!40000 ALTER TABLE `erp_employees` DISABLE KEYS */;
INSERT INTO `erp_employees` VALUES (2,NULL,1,13,12,NULL,'U011-NT-26-001','Mr.','Sri Ganesh','GANESH','Sudhagani','Sri Ganesh GANESH Sudhagani','MALE','2008-07-18',NULL,NULL,'uganda','hh','-','88','SINGLE','B_POSITIVE','HINDUISM','gouds','sudhaganisriganeshjk@gmail.com','ramesh@gmail.com','9154735655',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NON_TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-18','2027-07-24','2026-07-18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Teacher','English',NULL,0,1,'2','2026-07-18 18:18:31','2','2026-07-18 18:18:31',0),(3,NULL,1,14,13,NULL,'U011-T-26-001','Mr.','Sri Ganesh','GANESH','Sudhagani','Sri Ganesh GANESH Sudhagani','MALE','2008-07-18',NULL,NULL,'uganda','hh','-','88','SINGLE','B_NEGATIVE','HINDUISM','gouds','sriganeshgoud9154@gmail.com',NULL,'9154735659',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-18','2027-07-24','2026-07-18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'Teacher','English',NULL,0,1,'2','2026-07-18 18:22:41','2','2026-07-18 18:22:41',0),(4,NULL,1,2,12,NULL,'U011-NT-26-002','Ms.','bhanu',NULL,'bhanyu','bhanu bhanyu','FEMALE','2008-07-19',NULL,NULL,NULL,NULL,NULL,NULL,'SINGLE','A_NEGATIVE','HINDUISM','gouds',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NON_TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-19','2027-07-24','2026-07-12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 18:31:46','2','2026-07-18 18:31:46',0),(5,NULL,1,4,3,NULL,'U011-T-26-002','Ms.','bhanu',NULL,'bhanyu','bhanu bhanyu','FEMALE','2008-07-18',NULL,NULL,'uganda',NULL,NULL,NULL,'SINGLE','A_NEGATIVE','HINDUISM','gouds','ramesh@gmail.com','ramesh@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-19','2027-07-24','2026-07-12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 18:32:47','2','2026-07-18 18:32:47',0),(7,NULL,1,14,10,NULL,'U011-T-26-003','Mrs.','bhanu',NULL,'bhanyu','bhanu bhanyu','OTHER','2008-07-19',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'gouds','ramesh22@gmail.com','ramesh@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-19','2027-07-24','2026-07-12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 18:36:55','2','2026-07-18 18:36:55',0),(8,NULL,1,11,6,NULL,'U011-T-26-004','Ms.','bhanu',NULL,'bhanyu','bhanu bhanyu','MALE','2008-07-19',NULL,NULL,NULL,NULL,NULL,NULL,'SINGLE','O_POSITIVE','CHRISTIANITY','gouds','ramesh2de2@gmail.com','ramesh@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-19','2027-07-24','2026-07-12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 18:41:00','2','2026-07-18 18:41:00',0),(14,NULL,1,11,14,NULL,'U011-T-26-005',NULL,'Sri Ganesh',NULL,'Sudhagani','Sri Ganesh Sudhagani','MALE','2008-07-01',NULL,NULL,'uganda',NULL,NULL,NULL,'SINGLE','B_POSITIVE','ISLAM','gouds','sriganesh@gmail.com','sriganes9154@gmail.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-19','2027-07-24','2026-07-12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 19:12:06','2','2026-07-18 19:12:06',0),(17,NULL,1,1,14,NULL,'U011-T-26-006',NULL,'Sri Ganesh','GANESH','Sudhagani','Sri Ganesh GANESH Sudhagani','MALE','2008-07-01',NULL,NULL,'uganda',NULL,NULL,NULL,'DIVORCED','A_POSITIVE','ISLAM','gouds','sriganes9154@gmail.com',NULL,'09154763356',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-19','2027-07-24','2026-07-12',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 19:13:24','2','2026-07-18 19:13:24',0),(25,NULL,1,1,2,NULL,'U011-T-26-007','Mr.','Sri Ganesh','GANESH','Sudhagani','Sri Ganesh GANESH Sudhagani','MALE','2008-07-18',NULL,NULL,'uganda',NULL,NULL,NULL,NULL,NULL,NULL,NULL,'sriganeshgoudcsac@gmail.com','sriganeshgoud9154@gmail.com','091547683356',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 20:43:01','2','2026-07-18 20:43:01',0),(26,NULL,1,15,14,NULL,'U011-T-26-008','Ms.','Sri Ganesh','GANESH','Sudhagani','Sri Ganesh GANESH Sudhagani','FEMALE','2008-07-18',NULL,NULL,'uganda',NULL,NULL,NULL,'MARRIED','A_POSITIVE','OTHER',NULL,'sriganeshg4@gmail.com','sriganeshgoud9154@gmail.com','09154763356',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 21:25:44','2','2026-07-18 21:25:44',0),(27,NULL,1,12,11,NULL,'U011-T-26-009','Ms.','Sri Ganesh','GANESH','Sudhagani','Sri Ganesh GANESH Sudhagani','MALE','2008-07-18',NULL,NULL,'uganda',NULL,NULL,NULL,NULL,NULL,'ISLAM',NULL,'sriganeshszg4@gmail.com','sriganeshgoud9154@gmail.com','09154763356',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-18',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-18 21:29:34','2','2026-07-18 21:29:34',0),(39,NULL,1,4,5,NULL,'U011-T-26-010','Mr.','Sri Ganesh','GANESH','Sudhagani','Sri Ganesh GANESH Sudhagani','MALE','2008-07-18',NULL,NULL,'uganda',NULL,NULL,NULL,NULL,NULL,'HINDUISM',NULL,'filerollbacktest@example.com',NULL,'09154763356',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'TEACHING','TEMPORARY','FULL_TIME','ACTIVE','2026-07-19',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,1,'2','2026-07-19 16:07:21','2','2026-07-19 16:07:21',0);
/*!40000 ALTER TABLE `erp_employees` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_import_errors`
--

DROP TABLE IF EXISTS `erp_import_errors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_import_errors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `job_id` varchar(36) NOT NULL,
  `row_index` int(11) DEFAULT NULL,
  `column_name` varchar(100) DEFAULT NULL,
  `cell_value` varchar(1000) DEFAULT NULL,
  `error_code` varchar(20) NOT NULL,
  `severity` varchar(20) DEFAULT NULL,
  `message` varchar(1000) NOT NULL,
  `suggested_fix` varchar(1000) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_import_errors`
--

LOCK TABLES `erp_import_errors` WRITE;
/*!40000 ALTER TABLE `erp_import_errors` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_import_errors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_import_jobs`
--

DROP TABLE IF EXISTS `erp_import_jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_import_jobs` (
  `job_id` varchar(36) NOT NULL,
  `branch_id` varchar(36) DEFAULT NULL,
  `module` varchar(50) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `total_rows` int(11) DEFAULT 0,
  `processed_rows` int(11) DEFAULT 0,
  `success_rows` int(11) DEFAULT 0,
  `failed_rows` int(11) DEFAULT 0,
  `uploaded_file_name` varchar(255) DEFAULT NULL,
  `error_report_url` varchar(1000) DEFAULT NULL,
  `file_hash` varchar(64) DEFAULT NULL,
  `import_mode` varchar(20) NOT NULL,
  `last_checkpoint` varchar(500) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `completed_at` timestamp NULL DEFAULT NULL,
  `started_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_import_jobs`
--

LOCK TABLES `erp_import_jobs` WRITE;
/*!40000 ALTER TABLE `erp_import_jobs` DISABLE KEYS */;
INSERT INTO `erp_import_jobs` VALUES ('08153898-d52f-443d-82af-a89cd815431e','1','employee','FAILED',0,0,0,0,'55bd71d4-9371-4311-b52e-ed715fd8793a_Employee_Bulk_Import_.xlsx',NULL,'be1ef3e7bbd6dcc93ef993609b9b39e7dfc956121b28879fc9e1d4dd10dc9bd2','INSERT','ERROR: org.springframework.dao.InvalidDataAccessResourceUsageException: could not execute statement [You have an error in your SQL syntax; check the manual that corresponds to your MariaDB server version for the right syntax to use near \'row_number,severity,suggested_fix,updated_at,updated_by) values (\'N/A\',\'DATAB...\' at line 1] [insert into erp_import_errors (cell_value,column_name,created_at,created_by,error_code,job_id,message,row_number,severity,suggested_fix,updated_at,updated_by) values',NULL,'2026-07-15 13:30:57',NULL,'2026-07-15 13:31:03','2026-07-15 20:31:03','2026-07-15 20:30:58'),('12ee48ec-d416-42a7-adf1-a62ccdbae3ed','1','employee','FAILED',0,0,0,0,'1784115269270_Employee_Bulk_Import_.xlsx',NULL,'dummy-hash','INSERT',NULL,'1','2026-07-15 18:34:29','1','2026-07-15 18:34:37','2026-07-15 18:34:37','2026-07-15 18:34:29'),('144fdad0-4eea-4ef3-a8ce-8d574bce4e08','1','employee','FAILED',0,0,0,0,'1784116191010_Employee_Bulk_Import_.xlsx',NULL,'dummy-hash','INSERT',NULL,'1','2026-07-15 18:49:51','1','2026-07-15 18:49:59','2026-07-15 18:49:59','2026-07-15 18:49:51'),('19bb4750-91bb-4c66-9ba1-aaa0678754c5','1','employee','FAILED',0,0,0,0,'1784117570672_Employee_Bulk_Import_.xlsx',NULL,'hash-1784117570672','INSERT',NULL,'1','2026-07-15 19:12:50','1','2026-07-15 19:12:58','2026-07-15 19:12:58','2026-07-15 19:12:50'),('281a2bc4-7e16-4b77-bfbb-39f36f602d82','1','employee','FAILED',0,0,0,0,'896a0c1b-6c12-4c8f-9c85-7406959b5e56_Employee_Bulk_Import_.xlsx',NULL,'be1ef3e7bbd6dcc93ef993609b9b39e7dfc956121b28879fc9e1d4dd10dc9bd2','INSERT','ERROR: org.springframework.dao.InvalidDataAccessResourceUsageException: could not execute statement [You have an error in your SQL syntax; check the manual that corresponds to your MariaDB server version for the right syntax to use near \'row_number,severity,suggested_fix,updated_at,updated_by) values (\'N/A\',\'DATAB...\' at line 1] [insert into erp_import_errors (cell_value,column_name,created_at,created_by,error_code,job_id,message,row_number,severity,suggested_fix,updated_at,updated_by) values',NULL,'2026-07-16 11:21:21',NULL,'2026-07-16 11:21:32','2026-07-16 18:21:32','2026-07-16 18:21:20'),('6ae8ee55-9a89-483e-80c6-e77daa9aae54','1','employee','FAILED',0,0,0,0,'1784120008015_Employee_Bulk_Import_.xlsx',NULL,'hash-1784120008015','INSERT','File not found: C:\\Users\\sriga\\AppData\\Local\\Temp\\6ae8ee55-9a89-483e-80c6-e77daa9aae54_1784120008015_Employee_Bulk_Import_.xlsx',NULL,'2026-07-15 12:53:27',NULL,'2026-07-15 12:53:28','2026-07-15 19:53:29','2026-07-15 19:53:28'),('ad521a7b-dcab-493d-8771-9ed6dd67f5c0','1','employee','FAILED',0,0,0,0,'6e11f554-bc6b-46fc-bed9-d17ba2e4ade3_Employee_Bulk_Import_.xlsx',NULL,'hash-1784121670525','INSERT','ERROR: could not execute statement [Data truncation: Data too long for column \'status\' at row 1] [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,uploaded_file_name=? where job_id=?]; SQL [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,upload',NULL,'2026-07-15 13:21:09',NULL,'2026-07-15 13:21:26','2026-07-15 20:21:26','2026-07-15 20:21:10'),('b904c074-a444-40fa-9ca7-614a447f256b','1','employee','FAILED',0,0,0,0,'731a651f-cf1b-4c08-8505-1a8c53cadd38_Employee_Bulk_Import_.xlsx',NULL,'hash-1784120860301','INSERT','ERROR: could not execute statement [Data truncation: Data too long for column \'status\' at row 1] [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,uploaded_file_name=? where job_id=?]; SQL [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,upload',NULL,'2026-07-15 13:07:39',NULL,'2026-07-15 13:07:56','2026-07-15 20:07:56','2026-07-15 20:07:40'),('ca84bed8-eb45-4531-88b8-fca5b6c88808','1','employee','INITIALIZING',0,0,0,0,'Employee_Bulk_Import_.xlsx',NULL,'dummy-hash','INSERT',NULL,'1','2026-07-15 18:16:37','1','2026-07-15 18:16:38',NULL,'2026-07-15 18:16:37'),('d4d20c1b-9839-4ffe-af8c-acb503cb50c9','1','employee','INITIALIZING',0,0,0,0,'1784114674232_Employee_Bulk_Import_.xlsx',NULL,'dummy-hash','INSERT',NULL,'1','2026-07-15 18:24:34','1','2026-07-15 18:24:35',NULL,'2026-07-15 18:24:34'),('dbf96e81-7a74-4e85-b36c-c6a4d918bcca','1','employee','FAILED',0,0,0,0,'e3116ef2-9fd0-459f-bbee-6e27efab6e3d_Employee_Bulk_Import_.xlsx',NULL,'be1ef3e7bbd6dcc93ef993609b9b39e7dfc956121b28879fc9e1d4dd10dc9bd2','INSERT','ERROR: org.springframework.dao.InvalidDataAccessResourceUsageException: could not execute statement [You have an error in your SQL syntax; check the manual that corresponds to your MariaDB server version for the right syntax to use near \'row_number,severity,suggested_fix,updated_at,updated_by) values (\'N/A\',\'DATAB...\' at line 1] [insert into erp_import_errors (cell_value,column_name,created_at,created_by,error_code,job_id,message,row_number,severity,suggested_fix,updated_at,updated_by) values',NULL,'2026-07-15 13:30:10',NULL,'2026-07-15 13:30:18','2026-07-15 20:30:18','2026-07-15 20:30:10'),('e60829f3-c7ef-4449-9173-b5864966bbe6','1','employee','FAILED',0,0,0,0,'eb2ae700-aaa8-44b1-b993-43dff9386cbc_Employee_Bulk_Import_.xlsx',NULL,'hash-1784120394619','INSERT','ERROR: could not execute statement [Data truncation: Data too long for column \'status\' at row 1] [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,uploaded_file_name=? where job_id=?]; SQL [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,upload',NULL,'2026-07-15 12:59:54',NULL,'2026-07-15 13:00:10','2026-07-15 20:00:10','2026-07-15 19:59:54'),('e6faef07-3b8c-4a93-8936-a73bf6979f2f','1','employee','FAILED',0,0,0,0,'1784116852542_Employee_Bulk_Import_.xlsx',NULL,'dummy-hash','INSERT',NULL,'1','2026-07-15 19:00:52','1','2026-07-15 19:00:59','2026-07-15 19:00:59','2026-07-15 19:00:52'),('e8ce1ae2-0e89-4fb7-a4a8-6e315faf9242','1','employee','FAILED',0,0,0,0,'cd920d63-8c4a-46fe-8d42-53152af7b216_Employee_Bulk_Import_.xlsx',NULL,'hash-1784121016622','INSERT','ERROR: could not execute statement [Data truncation: Data too long for column \'status\' at row 1] [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,uploaded_file_name=? where job_id=?]; SQL [update erp_import_jobs set branch_id=?,completed_at=?,failed_rows=?,file_hash=?,import_mode=?,last_checkpoint=?,module=?,processed_rows=?,started_at=?,status=?,success_rows=?,upload',NULL,'2026-07-15 13:10:15',NULL,'2026-07-15 13:10:29','2026-07-15 20:10:30','2026-07-15 20:10:16');
/*!40000 ALTER TABLE `erp_import_jobs` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `erp_levels`
--

LOCK TABLES `erp_levels` WRITE;
/*!40000 ALTER TABLE `erp_levels` DISABLE KEYS */;
INSERT INTO `erp_levels` VALUES (1,'Nursery',1,1,'2026-06-29 14:35:26','2026-06-29 14:35:26',NULL,NULL),(2,'Primary',2,1,'2026-06-29 14:35:26','2026-06-29 14:35:26',NULL,NULL),(3,'Secondary',3,1,'2026-06-29 14:35:26','2026-06-29 14:35:26',NULL,NULL),(4,'Senior Secondary',4,1,'2026-06-29 14:49:30','2026-06-29 14:49:30',NULL,NULL);
/*!40000 ALTER TABLE `erp_levels` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_login_history`
--

DROP TABLE IF EXISTS `erp_login_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_login_history` (
  `login_history_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `branch_id` int(11) DEFAULT NULL,
  `login_time` datetime NOT NULL,
  `logout_time` datetime DEFAULT NULL,
  `ip_address` varchar(100) DEFAULT NULL,
  `device_name` varchar(255) DEFAULT NULL,
  `browser_name` varchar(255) DEFAULT NULL,
  `login_status` enum('FAILED','LOCKED','LOGOUT','SUCCESS') NOT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`login_history_id`),
  KEY `idx_loginhistory_user` (`user_id`),
  KEY `idx_loginhistory_branch` (`branch_id`),
  KEY `idx_loginhistory_status` (`login_status`),
  KEY `idx_loginhistory_time` (`login_time`),
  CONSTRAINT `fk_loginhistory_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_loginhistory_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_login_history`
--

LOCK TABLES `erp_login_history` WRITE;
/*!40000 ALTER TABLE `erp_login_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_login_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_parents`
--

DROP TABLE IF EXISTS `erp_parents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_parents` (
  `parent_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `father_name` varchar(150) DEFAULT NULL,
  `father_uin` varchar(20) DEFAULT NULL,
  `father_phone` varchar(30) DEFAULT NULL,
  `father_alternate_phone` varchar(30) DEFAULT NULL,
  `father_email` varchar(150) DEFAULT NULL,
  `father_occupation` varchar(150) DEFAULT NULL,
  `father_employer` varchar(200) DEFAULT NULL,
  `father_designation` varchar(150) DEFAULT NULL,
  `father_annual_income` decimal(15,2) DEFAULT NULL,
  `mother_name` varchar(150) DEFAULT NULL,
  `mother_uin` varchar(20) DEFAULT NULL,
  `mother_phone` varchar(30) DEFAULT NULL,
  `mother_alternate_phone` varchar(30) DEFAULT NULL,
  `mother_email` varchar(150) DEFAULT NULL,
  `mother_occupation` varchar(150) DEFAULT NULL,
  `mother_employer` varchar(200) DEFAULT NULL,
  `mother_designation` varchar(150) DEFAULT NULL,
  `mother_annual_income` decimal(15,2) DEFAULT NULL,
  `guardian_name` varchar(150) DEFAULT NULL,
  `guardian_uin` varchar(20) DEFAULT NULL,
  `guardian_relationship` varchar(100) DEFAULT NULL,
  `guardian_phone` varchar(30) DEFAULT NULL,
  `guardian_alternate_phone` varchar(30) DEFAULT NULL,
  `guardian_email` varchar(150) DEFAULT NULL,
  `guardian_occupation` varchar(150) DEFAULT NULL,
  `preferred_contact` enum('FATHER','GUARDIAN','MOTHER') NOT NULL,
  `fee_responsibility` enum('FATHER','GUARDIAN','MOTHER','SPONSOR') NOT NULL,
  `parents_living_together` tinyint(1) NOT NULL DEFAULT 1,
  `emergency_contact_name` varchar(150) DEFAULT NULL,
  `emergency_contact_phone` varchar(30) DEFAULT NULL,
  `emergency_contact_relationship` varchar(100) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` text DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`parent_id`),
  UNIQUE KEY `uk_parent_student` (`student_id`),
  KEY `idx_parent_branch` (`branch_id`),
  KEY `idx_parent_admission` (`admission_no`),
  KEY `idx_father_phone` (`father_phone`),
  KEY `idx_mother_phone` (`mother_phone`),
  KEY `idx_guardian_phone` (`guardian_phone`),
  KEY `idx_father_uin` (`father_uin`),
  KEY `idx_mother_uin` (`mother_uin`),
  KEY `idx_guardian_uin` (`guardian_uin`),
  KEY `idx_active` (`active`),
  KEY `idx_father_email` (`father_email`),
  KEY `idx_mother_email` (`mother_email`),
  KEY `idx_guardian_email` (`guardian_email`),
  KEY `idx_emergency_phone` (`emergency_contact_phone`),
  CONSTRAINT `fk_parent_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_parent_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_parents`
--

LOCK TABLES `erp_parents` WRITE;
/*!40000 ALTER TABLE `erp_parents` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_parents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_permissions`
--

DROP TABLE IF EXISTS `erp_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_permissions` (
  `permission_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `permission_code` varchar(100) NOT NULL,
  `permission_name` varchar(150) NOT NULL,
  `module_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_permissions`
--

LOCK TABLES `erp_permissions` WRITE;
/*!40000 ALTER TABLE `erp_permissions` DISABLE KEYS */;
INSERT INTO `erp_permissions` VALUES (1,'APPLICATION_VIEW','View Applications','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(2,'APPLICATION_CREATE','Create Application','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(3,'APPLICATION_EDIT','Edit Application','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(4,'APPLICATION_DELETE','Delete Application','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(5,'APPLICATION_VERIFY','Verify Application','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(6,'APPLICATION_APPROVE','Approve Application','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(7,'APPLICATION_REJECT','Reject Application','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(8,'APPLICATION_INTERVIEW','Manage Interviews','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(9,'APPLICATION_DOCUMENT_MANAGE','Manage Application Documents','Application',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(10,'SCHOLARSHIP_VIEW','View Scholarships','Scholarship',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(11,'SCHOLARSHIP_CREATE','Create Scholarship','Scholarship',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(12,'SCHOLARSHIP_EDIT','Edit Scholarship','Scholarship',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(13,'SCHOLARSHIP_APPROVE','Approve Scholarship','Scholarship',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(14,'SCHOLARSHIP_REJECT','Reject Scholarship','Scholarship',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(15,'SCHOLARSHIP_DOCUMENT_MANAGE','Manage Scholarship Documents','Scholarship',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(16,'SCHOLARSHIP_ALLOCATE','Allocate Scholarship','Scholarship',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(17,'STUDENT_VIEW','View Students','Student',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(18,'STUDENT_CREATE','Create Student','Student',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(19,'STUDENT_EDIT','Edit Student','Student',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(20,'STUDENT_DELETE','Delete Student','Student',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(21,'STUDENT_TRANSFER','Transfer Student','Student',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(22,'STUDENT_GRADUATE','Graduate Student','Student',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(23,'FEE_VIEW','View Fees','Fee',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(24,'FEE_COLLECT','Collect Fees','Fee',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(25,'FEE_REFUND','Refund Fees','Fee',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(26,'FEE_CONCESSION','Manage Fee Concessions','Fee',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(27,'FEE_REPORT','View Fee Reports','Fee',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(28,'ACADEMIC_YEAR_VIEW','View Academic Years','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(29,'ACADEMIC_YEAR_MANAGE','Manage Academic Years','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(30,'CLASS_VIEW','View Classes','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(31,'CLASS_MANAGE','Manage Classes','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(32,'SECTION_VIEW','View Sections','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(33,'SECTION_MANAGE','Manage Sections','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(34,'SUBJECT_VIEW','View Subjects','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(35,'SUBJECT_MANAGE','Manage Subjects','Academic',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(36,'DEPARTMENT_VIEW','View Departments','HR',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(37,'DEPARTMENT_MANAGE','Manage Departments','HR',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(38,'DESIGNATION_VIEW','View Designations','HR',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(39,'DESIGNATION_MANAGE','Manage Designations','HR',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(40,'EMPLOYEE_VIEW','View Employees','HR',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(41,'EMPLOYEE_MANAGE','Manage Employees','HR',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(42,'EXAM_VIEW','View Exams','Exam',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(43,'EXAM_MANAGE','Manage Exams','Exam',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(44,'USER_VIEW','View Users','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(45,'USER_CREATE','Create Users','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(46,'USER_EDIT','Edit Users','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(47,'USER_DELETE','Delete Users','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(48,'ROLE_VIEW','View Roles','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(49,'ROLE_MANAGE','Manage Roles','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(50,'PERMISSION_VIEW','View Permissions','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(51,'PERMISSION_MANAGE','Manage Permissions','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(52,'ROLE_PERMISSION_MANAGE','Manage Role Permissions','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(53,'USER_ROLE_MANAGE','Manage User Roles','Security',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(54,'BRANCH_VIEW','View Branches','System',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(55,'BRANCH_MANAGE','Manage Branches','System',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(56,'SYSTEM_SETTINGS','Manage System Settings','System',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(57,'AUDIT_VIEW','View Audit Logs','System',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06'),(58,'REPORT_VIEW','View Reports','Reports',NULL,1,0,NULL,'2026-07-06 09:51:06',NULL,'2026-07-06 09:51:06');
/*!40000 ALTER TABLE `erp_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_role_permissions`
--

DROP TABLE IF EXISTS `erp_role_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_role_permissions` (
  `role_permission_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`role_permission_id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `fk_rolepermission_permission` (`permission_id`),
  CONSTRAINT `fk_rolepermission_permission` FOREIGN KEY (`permission_id`) REFERENCES `erp_permissions` (`permission_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_rolepermission_role` FOREIGN KEY (`role_id`) REFERENCES `erp_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_role_permissions`
--

LOCK TABLES `erp_role_permissions` WRITE;
/*!40000 ALTER TABLE `erp_role_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_role_permissions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_roles`
--

DROP TABLE IF EXISTS `erp_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_roles` (
  `role_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) NOT NULL,
  `role_name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_roles`
--

LOCK TABLES `erp_roles` WRITE;
/*!40000 ALTER TABLE `erp_roles` DISABLE KEYS */;
INSERT INTO `erp_roles` VALUES (1,'SUPER_ADMIN','Super Admin','Full system access',1,0,NULL,'2026-07-06 09:35:05',NULL,'2026-07-08 04:27:08'),(2,'ERP_ADMIN','ERP Admin','Manage schools and system settings',1,0,NULL,'2026-07-06 09:35:05',NULL,'2026-07-08 04:27:08'),(3,'BRANCH_ADMIN','Branch Admin','Manage branch operations',1,0,NULL,'2026-07-08 04:27:08',NULL,'2026-07-08 04:27:08');
/*!40000 ALTER TABLE `erp_roles` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `erp_scholarship_allocations`
--

LOCK TABLES `erp_scholarship_allocations` WRITE;
/*!40000 ALTER TABLE `erp_scholarship_allocations` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_scholarship_allocations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_scholarship_application_docs`
--

DROP TABLE IF EXISTS `erp_scholarship_application_docs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_application_docs` (
  `document_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `scholarship_app_id` bigint(20) NOT NULL,
  `document_type` varchar(50) NOT NULL,
  `verification_status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `original_file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint(20) DEFAULT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_hash` varchar(64) DEFAULT NULL,
  `uploaded_by` bigint(20) DEFAULT NULL,
  `uploaded_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`document_id`),
  KEY `idx_document_scholarship` (`scholarship_app_id`),
  CONSTRAINT `fk_scholarship_doc_app` FOREIGN KEY (`scholarship_app_id`) REFERENCES `erp_scholarship_applications` (`scholarship_app_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_scholarship_application_docs`
--

LOCK TABLES `erp_scholarship_application_docs` WRITE;
/*!40000 ALTER TABLE `erp_scholarship_application_docs` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_scholarship_application_docs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_scholarship_applications`
--

DROP TABLE IF EXISTS `erp_scholarship_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_scholarship_applications` (
  `scholarship_app_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL DEFAULT 1,
  `application_id` bigint(20) DEFAULT NULL,
  `student_id` bigint(20) DEFAULT NULL,
  `academic_year` varchar(20) NOT NULL,
  `amount_requested_ugx` decimal(38,2) NOT NULL DEFAULT 0.00,
  `term_requested` varchar(50) NOT NULL DEFAULT 'TERM_1',
  `category` varchar(100) NOT NULL DEFAULT 'GENERAL',
  `scholarship_type` enum('MERIT','NEED_BASED','SPORTS','STAFF_CHILD','SIBLING','DONOR','OTHER') DEFAULT 'OTHER',
  `requested_percentage` decimal(5,2) NOT NULL,
  `approved_percentage` decimal(5,2) DEFAULT NULL,
  `approved_amount` decimal(12,2) DEFAULT NULL,
  `valid_until` date DEFAULT NULL,
  `parent_income_declared` decimal(15,2) DEFAULT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `reviewer_remarks` varchar(500) DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Pending',
  `reviewed_by` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`scholarship_app_id`),
  UNIQUE KEY `uk_scholarship_application` (`application_id`),
  UNIQUE KEY `uk_scholarship_student` (`student_id`),
  KEY `idx_scholarship_application` (`application_id`),
  KEY `idx_scholarship_student` (`student_id`),
  KEY `idx_scholarship_status` (`status`),
  KEY `idx_scholarship_year` (`academic_year`),
  CONSTRAINT `fk_scholarship_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_scholarship_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_scholarship_applications`
--

LOCK TABLES `erp_scholarship_applications` WRITE;
/*!40000 ALTER TABLE `erp_scholarship_applications` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_scholarship_applications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_sections`
--

DROP TABLE IF EXISTS `erp_sections`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_sections` (
  `section_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` int(11) NOT NULL,
  `academic_year_id` bigint(20) NOT NULL,
  `class_id` int(11) NOT NULL,
  `section_code` varchar(20) NOT NULL,
  `section_name` varchar(100) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT 40,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`section_id`),
  UNIQUE KEY `uk_branch_year_class_section` (`branch_id`,`academic_year_id`,`class_id`,`section_code`),
  KEY `fk_section_year` (`academic_year_id`),
  KEY `fk_section_class` (`class_id`),
  CONSTRAINT `fk_section_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_section_class` FOREIGN KEY (`class_id`) REFERENCES `erp_classes` (`class_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_section_year` FOREIGN KEY (`academic_year_id`) REFERENCES `erp_academic_years` (`academic_year_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_sections`
--

LOCK TABLES `erp_sections` WRITE;
/*!40000 ALTER TABLE `erp_sections` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_sections` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_site_settings`
--

DROP TABLE IF EXISTS `erp_site_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_site_settings` (
  `setting_id` int(11) NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) DEFAULT NULL,
  `setting_value` text DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`setting_id`),
  UNIQUE KEY `setting_key` (`setting_key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_site_settings`
--

LOCK TABLES `erp_site_settings` WRITE;
/*!40000 ALTER TABLE `erp_site_settings` DISABLE KEYS */;
INSERT INTO `erp_site_settings` VALUES (1,'academic_year','2026-2027',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `erp_site_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_academic_history`
--

DROP TABLE IF EXISTS `erp_student_academic_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_academic_history` (
  `academic_history_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `former_school_name` varchar(255) DEFAULT NULL,
  `former_school_code` varchar(50) DEFAULT NULL,
  `former_school_lin` varchar(50) DEFAULT NULL,
  `former_school_address` varchar(255) DEFAULT NULL,
  `school_type` enum('GOVERNMENT','INTERNATIONAL','OTHER','PRIVATE') DEFAULT NULL,
  `transfer_reason` varchar(255) DEFAULT NULL,
  `previous_academic_year` varchar(20) DEFAULT NULL,
  `previous_class` varchar(50) DEFAULT NULL,
  `previous_section` varchar(50) DEFAULT NULL,
  `previous_stream` varchar(50) DEFAULT NULL,
  `ple_index_number` varchar(50) DEFAULT NULL,
  `ple_aggregate` varchar(20) DEFAULT NULL,
  `uce_index_number` varchar(50) DEFAULT NULL,
  `uce_result` varchar(50) DEFAULT NULL,
  `uace_index_number` varchar(50) DEFAULT NULL,
  `uace_result` varchar(50) DEFAULT NULL,
  `subject_marks` longtext DEFAULT NULL,
  `previous_report_card` varchar(255) DEFAULT NULL,
  `transfer_certificate` varchar(255) DEFAULT NULL,
  `leaving_certificate` varchar(255) DEFAULT NULL,
  `verification_status` enum('PENDING','REJECTED','VERIFIED') NOT NULL,
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_at` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` longtext DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`academic_history_id`),
  UNIQUE KEY `uk_student_academic_history` (`student_id`),
  KEY `idx_academic_student` (`student_id`),
  KEY `idx_academic_branch` (`branch_id`),
  KEY `idx_academic_admission` (`admission_no`),
  KEY `idx_academic_school` (`former_school_code`),
  KEY `idx_academic_verification` (`verification_status`),
  KEY `idx_branch_ple` (`branch_id`,`ple_index_number`),
  KEY `idx_branch_uce` (`branch_id`,`uce_index_number`),
  KEY `idx_branch_uace` (`branch_id`,`uace_index_number`),
  KEY `idx_ple` (`ple_index_number`),
  KEY `idx_uce` (`uce_index_number`),
  KEY `idx_uace` (`uace_index_number`),
  CONSTRAINT `fk_academic_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_academic_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_academic_history`
--

LOCK TABLES `erp_student_academic_history` WRITE;
/*!40000 ALTER TABLE `erp_student_academic_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_academic_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_accounts`
--

DROP TABLE IF EXISTS `erp_student_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_accounts` (
  `account_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `account_status` enum('ACTIVE','DISABLED','LOCKED','SUSPENDED') NOT NULL,
  `password_changed` tinyint(1) NOT NULL DEFAULT 0,
  `password_reset_required` tinyint(1) NOT NULL DEFAULT 0,
  `failed_attempts` int(11) NOT NULL DEFAULT 0,
  `account_locked` tinyint(1) NOT NULL DEFAULT 0,
  `last_login` datetime DEFAULT NULL,
  `last_login_ip` varchar(100) DEFAULT NULL,
  `last_login_device` varchar(255) DEFAULT NULL,
  `last_password_change` datetime DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` text DEFAULT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `uk_student_username` (`username`),
  UNIQUE KEY `uk_student_account` (`student_id`),
  KEY `idx_student_account_student` (`student_id`),
  KEY `idx_student_account_branch` (`branch_id`),
  KEY `idx_student_account_admission` (`admission_no`),
  CONSTRAINT `fk_student_account_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_student_account_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_accounts`
--

LOCK TABLES `erp_student_accounts` WRITE;
/*!40000 ALTER TABLE `erp_student_accounts` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_accounts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_alumni`
--

DROP TABLE IF EXISTS `erp_student_alumni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_alumni` (
  `alumni_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `graduation_year` int(11) NOT NULL,
  `graduation_date` date DEFAULT NULL,
  `final_class` varchar(50) DEFAULT NULL,
  `final_stream` varchar(50) DEFAULT NULL,
  `final_grade` varchar(50) DEFAULT NULL,
  `certificate_number` varchar(100) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`alumni_id`),
  UNIQUE KEY `uk_alumni_student` (`student_id`),
  KEY `idx_alumni_branch` (`branch_id`),
  KEY `idx_alumni_admission` (`admission_no`),
  KEY `idx_alumni_grad_year` (`graduation_year`),
  KEY `idx_alumni_graduation_date` (`graduation_date`),
  KEY `idx_alumni_certificate` (`certificate_number`),
  KEY `idx_alumni_student` (`student_id`),
  CONSTRAINT `fk_alumni_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_alumni_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_alumni`
--

LOCK TABLES `erp_student_alumni` WRITE;
/*!40000 ALTER TABLE `erp_student_alumni` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_alumni` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_archives`
--

DROP TABLE IF EXISTS `erp_student_archives`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_archives` (
  `archive_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `archive_status` enum('ARCHIVED','RESTORED') NOT NULL,
  `archive_reason` enum('DECEASED','DROPPED_OUT','EXPELLED','GRADUATED','OTHER','TRANSFERRED','WITHDRAWN') NOT NULL,
  `date_of_leaving` date NOT NULL,
  `restored_by` bigint(20) DEFAULT NULL,
  `restored_at` datetime DEFAULT NULL,
  `restore_reason` varchar(255) DEFAULT NULL,
  `remarks` tinytext DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`archive_id`),
  KEY `idx_archive_student` (`student_id`),
  KEY `idx_archive_branch` (`branch_id`),
  KEY `idx_archive_admission` (`admission_no`),
  KEY `idx_archive_status` (`archive_status`),
  KEY `idx_archive_reason` (`archive_reason`),
  KEY `idx_archive_leaving_date` (`date_of_leaving`),
  CONSTRAINT `fk_archive_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_archive_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_archives`
--

LOCK TABLES `erp_student_archives` WRITE;
/*!40000 ALTER TABLE `erp_student_archives` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_archives` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_documents`
--

DROP TABLE IF EXISTS `erp_student_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_documents` (
  `document_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `document_type` varchar(100) NOT NULL,
  `document_name` varchar(150) NOT NULL,
  `document_number` varchar(100) DEFAULT NULL,
  `file_name` varchar(255) NOT NULL,
  `original_file_name` varchar(255) DEFAULT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_extension` varchar(20) DEFAULT NULL,
  `mime_type` varchar(100) DEFAULT NULL,
  `file_size` bigint(20) DEFAULT NULL,
  `document_status` enum('PENDING','VERIFIED','REJECTED','EXPIRED') DEFAULT 'PENDING',
  `remarks` tinytext DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `uploaded_by` bigint(20) DEFAULT NULL,
  `uploaded_at` timestamp NULL DEFAULT current_timestamp(),
  `verified_by` bigint(20) DEFAULT NULL,
  `verified_at` timestamp NULL DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`document_id`),
  KEY `idx_student_documents_student` (`student_id`),
  KEY `idx_student_documents_branch` (`branch_id`),
  KEY `idx_student_documents_status` (`document_status`),
  KEY `idx_student_documents_type` (`document_type`),
  KEY `idx_student_documents_admission_no` (`admission_no`),
  CONSTRAINT `fk_student_documents_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_student_documents_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_documents`
--

LOCK TABLES `erp_student_documents` WRITE;
/*!40000 ALTER TABLE `erp_student_documents` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_documents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_enrollment`
--

DROP TABLE IF EXISTS `erp_student_enrollment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_enrollment` (
  `enrollment_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `academic_year_id` bigint(20) NOT NULL,
  `class_id` bigint(20) NOT NULL,
  `section_id` bigint(20) DEFAULT NULL,
  `stream_id` bigint(20) DEFAULT NULL,
  `house_id` bigint(20) DEFAULT NULL,
  `hostel_id` bigint(20) DEFAULT NULL,
  `bed_id` bigint(20) DEFAULT NULL,
  `roll_no` varchar(20) DEFAULT NULL,
  `admission_type` enum('NEW','READMISSION','TRANSFER') NOT NULL,
  `promotion_type` enum('NEW','PROMOTED','RETAINED','TRANSFERRED') NOT NULL,
  `enrollment_status` enum('ACTIVE','EXPELLED','GRADUATED','PROMOTED','SUSPENDED','TRANSFERRED','WITHDRAWN') NOT NULL,
  `joining_date` date NOT NULL,
  `leaving_date` date DEFAULT NULL,
  `class_teacher_id` bigint(20) DEFAULT NULL,
  `fee_structure_id` bigint(20) DEFAULT NULL,
  `scholarship_id` bigint(20) DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `is_locked` tinyint(1) NOT NULL DEFAULT 0,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` tinytext DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `uk_student_current` (`student_id`),
  KEY `idx_enrollment_student` (`student_id`),
  KEY `idx_enrollment_branch` (`branch_id`),
  KEY `idx_enrollment_admission` (`admission_no`),
  KEY `idx_enrollment_year` (`academic_year_id`),
  KEY `idx_enrollment_class` (`class_id`),
  KEY `idx_enrollment_status` (`enrollment_status`),
  CONSTRAINT `fk_enrollment_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_enrollment_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_enrollment`
--

LOCK TABLES `erp_student_enrollment` WRITE;
/*!40000 ALTER TABLE `erp_student_enrollment` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_enrollment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_enrollment_history`
--

DROP TABLE IF EXISTS `erp_student_enrollment_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_enrollment_history` (
  `enrollment_history_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `enrollment_id` bigint(20) DEFAULT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year_id` bigint(20) NOT NULL,
  `class_id` bigint(20) NOT NULL,
  `section_id` bigint(20) DEFAULT NULL,
  `stream_id` bigint(20) DEFAULT NULL,
  `house_id` bigint(20) DEFAULT NULL,
  `hostel_id` bigint(20) DEFAULT NULL,
  `bed_id` bigint(20) DEFAULT NULL,
  `roll_no` varchar(20) DEFAULT NULL,
  `admission_type` varchar(20) NOT NULL,
  `promotion_type` varchar(20) NOT NULL,
  `enrollment_status` varchar(20) NOT NULL,
  `joining_date` date NOT NULL,
  `leaving_date` date DEFAULT NULL,
  `effective_date` date NOT NULL,
  `change_reason` varchar(255) DEFAULT NULL,
  `remarks` tinytext DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `approved_at` datetime DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`enrollment_history_id`),
  KEY `idx_hist_student` (`student_id`),
  KEY `idx_hist_enrollment` (`enrollment_id`),
  KEY `idx_hist_branch` (`branch_id`),
  KEY `idx_hist_admission` (`admission_no`),
  KEY `idx_hist_year` (`academic_year_id`),
  KEY `idx_hist_class` (`class_id`),
  KEY `idx_hist_status` (`enrollment_status`),
  KEY `idx_hist_effective_date` (`effective_date`),
  KEY `idx_hist_branch_year` (`branch_id`,`academic_year_id`),
  CONSTRAINT `fk_hist_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_hist_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `erp_student_enrollment` (`enrollment_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_hist_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_enrollment_history`
--

LOCK TABLES `erp_student_enrollment_history` WRITE;
/*!40000 ALTER TABLE `erp_student_enrollment_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_enrollment_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_fee_assignments`
--

DROP TABLE IF EXISTS `erp_student_fee_assignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_fee_assignments` (
  `fee_assignment_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `term` varchar(30) NOT NULL,
  `fee_name` varchar(150) NOT NULL,
  `fee_type` varchar(50) NOT NULL,
  `total_fee` decimal(12,2) NOT NULL,
  `scholarship_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `concession_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `fine_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `payable_amount` decimal(12,2) NOT NULL,
  `paid_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `balance_amount` decimal(12,2) NOT NULL,
  `assignment_date` date NOT NULL,
  `due_date` date DEFAULT NULL,
  `fee_status` enum('CANCELLED','OVERDUE','PAID','PARTIAL','PENDING') NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` tinytext DEFAULT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`fee_assignment_id`),
  UNIQUE KEY `uk_student_fee_assignment` (`student_id`,`academic_year`,`term`,`fee_name`),
  KEY `idx_fee_assignment_student` (`student_id`),
  KEY `idx_fee_assignment_branch` (`branch_id`),
  KEY `idx_fee_assignment_admission` (`admission_no`),
  KEY `idx_fee_assignment_year` (`academic_year`),
  KEY `idx_fee_assignment_term` (`term`),
  KEY `idx_fee_assignment_status` (`fee_status`),
  KEY `idx_fee_assignment_due_date` (`due_date`),
  KEY `idx_fee_assignment_fee_name` (`fee_name`),
  CONSTRAINT `fk_fee_assignment_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_assignment_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_fee_assignments`
--

LOCK TABLES `erp_student_fee_assignments` WRITE;
/*!40000 ALTER TABLE `erp_student_fee_assignments` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_fee_assignments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_fee_ledger`
--

DROP TABLE IF EXISTS `erp_student_fee_ledger`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_fee_ledger` (
  `fee_ledger_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fee_assignment_id` bigint(20) NOT NULL,
  `fee_receipt_id` bigint(20) DEFAULT NULL,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `term` varchar(30) NOT NULL,
  `fee_name` varchar(150) NOT NULL,
  `fee_type` varchar(50) NOT NULL,
  `transaction_type` enum('ADJUSTMENT','CONCESSION','DISCOUNT','FEE_ASSIGNED','FINE','PARTIAL_PAYMENT','PAYMENT','REFUND','REVERSAL','SCHOLARSHIP','WAIVER') NOT NULL,
  `payment_mode` enum('BANK_TRANSFER','CASH','CHEQUE','CREDIT_CARD','DEBIT_CARD','MOBILE_MONEY','ONLINE','SCHOLARSHIP','WAIVER') DEFAULT NULL,
  `transaction_reference` varchar(150) DEFAULT NULL,
  `transaction_date_time` datetime NOT NULL,
  `debit_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `credit_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `running_balance` decimal(12,2) NOT NULL,
  `currency` char(3) NOT NULL DEFAULT 'UGX',
  `ledger_status` enum('ACTIVE','CANCELLED','REVERSED') NOT NULL,
  `remarks` text DEFAULT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`fee_ledger_id`),
  KEY `idx_fee_ledger_assignment` (`fee_assignment_id`),
  KEY `idx_fee_ledger_receipt` (`fee_receipt_id`),
  KEY `idx_fee_ledger_student` (`student_id`),
  KEY `idx_fee_ledger_branch` (`branch_id`),
  KEY `idx_fee_ledger_admission` (`admission_no`),
  KEY `idx_fee_ledger_transaction` (`transaction_type`),
  KEY `idx_fee_ledger_date` (`transaction_date_time`),
  KEY `idx_fee_ledger_status` (`ledger_status`),
  KEY `idx_fee_ledger_year_term` (`academic_year`,`term`),
  KEY `idx_fee_ledger_fee_name` (`fee_name`),
  KEY `idx_fee_ledger_student_date` (`student_id`,`transaction_date_time`),
  KEY `idx_fee_ledger_assignment_date` (`fee_assignment_id`,`transaction_date_time`),
  CONSTRAINT `fk_fee_ledger_assignment` FOREIGN KEY (`fee_assignment_id`) REFERENCES `erp_student_fee_assignments` (`fee_assignment_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_ledger_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_ledger_receipt` FOREIGN KEY (`fee_receipt_id`) REFERENCES `erp_student_fee_payments` (`fee_receipt_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_ledger_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_fee_ledger_currency` CHECK (`currency` = 'UGX'),
  CONSTRAINT `chk_fee_ledger_debit` CHECK (`debit_amount` >= 0),
  CONSTRAINT `chk_fee_ledger_credit` CHECK (`credit_amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_fee_ledger`
--

LOCK TABLES `erp_student_fee_ledger` WRITE;
/*!40000 ALTER TABLE `erp_student_fee_ledger` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_fee_ledger` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_fee_payments`
--

DROP TABLE IF EXISTS `erp_student_fee_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_fee_payments` (
  `fee_receipt_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fee_assignment_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `receipt_no` varchar(150) NOT NULL,
  `payment_date_time` datetime NOT NULL,
  `payment_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `excess_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `payment_mode` enum('BANK_TRANSFER','CASH','CHEQUE','CREDIT_CARD','DEBIT_CARD','MOBILE_MONEY','ONLINE','SCHOLARSHIP','WAIVER') NOT NULL,
  `transaction_reference` varchar(150) DEFAULT NULL,
  `collection_point` varchar(100) DEFAULT NULL,
  `payment_status` enum('CANCELLED','FAILED','PENDING','REFUNDED','REVERSED','SUCCESS') NOT NULL,
  `receipt_printed` tinyint(1) NOT NULL DEFAULT 0,
  `collected_by` bigint(20) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `remarks` longtext DEFAULT NULL,
  `cancel_reason` longtext DEFAULT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`fee_receipt_id`),
  UNIQUE KEY `uk_fee_payment_receipt` (`receipt_no`),
  KEY `idx_fee_payment_assignment` (`fee_assignment_id`),
  KEY `idx_fee_payment_student` (`student_id`),
  KEY `idx_fee_payment_branch` (`branch_id`),
  KEY `idx_fee_payment_admission` (`admission_no`),
  KEY `idx_fee_payment_date` (`payment_date_time`),
  KEY `idx_fee_payment_status` (`payment_status`),
  KEY `idx_fee_payment_receipt` (`receipt_no`),
  CONSTRAINT `fk_fee_payment_assignment` FOREIGN KEY (`fee_assignment_id`) REFERENCES `erp_student_fee_assignments` (`fee_assignment_id`),
  CONSTRAINT `fk_fee_payment_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`),
  CONSTRAINT `fk_fee_payment_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_fee_payments`
--

LOCK TABLES `erp_student_fee_payments` WRITE;
/*!40000 ALTER TABLE `erp_student_fee_payments` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_fee_payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_hostel`
--

DROP TABLE IF EXISTS `erp_student_hostel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_hostel` (
  `hostel_allocation_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `hostel_id` bigint(20) NOT NULL,
  `room_id` bigint(20) DEFAULT NULL,
  `bed_id` bigint(20) DEFAULT NULL,
  `allocation_start_date` date NOT NULL,
  `allocation_end_date` date DEFAULT NULL,
  `monthly_fee` decimal(12,2) NOT NULL DEFAULT 0.00,
  `annual_fee` decimal(12,2) NOT NULL DEFAULT 0.00,
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `payable_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `allocation_status` enum('ACTIVE','CANCELLED','INACTIVE','SUSPENDED','VACATED') NOT NULL,
  `payment_status` enum('PAID','PARTIAL','PENDING') NOT NULL,
  `local_guardian_name` varchar(150) DEFAULT NULL,
  `local_guardian_mobile` varchar(20) DEFAULT NULL,
  `local_guardian_relation` varchar(50) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`hostel_allocation_id`),
  UNIQUE KEY `uk_student_hostel_allocation` (`student_id`,`academic_year`),
  KEY `idx_hostel_student` (`student_id`),
  KEY `idx_hostel_branch` (`branch_id`),
  KEY `idx_hostel_master` (`hostel_id`),
  KEY `idx_hostel_room` (`room_id`),
  KEY `idx_hostel_bed` (`bed_id`),
  KEY `idx_hostel_status` (`allocation_status`),
  KEY `idx_hostel_payment` (`payment_status`),
  KEY `idx_hostel_year` (`academic_year`),
  KEY `idx_hostel_admission` (`admission_no`),
  CONSTRAINT `fk_hostel_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_hostel_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_hostel_monthly_fee` CHECK (`monthly_fee` >= 0),
  CONSTRAINT `chk_hostel_annual_fee` CHECK (`annual_fee` >= 0),
  CONSTRAINT `chk_hostel_discount` CHECK (`discount_amount` >= 0),
  CONSTRAINT `chk_hostel_payable` CHECK (`payable_amount` >= 0),
  CONSTRAINT `chk_hostel_dates` CHECK (`allocation_end_date` is null or `allocation_end_date` >= `allocation_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_hostel`
--

LOCK TABLES `erp_student_hostel` WRITE;
/*!40000 ALTER TABLE `erp_student_hostel` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_hostel` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_medical`
--

DROP TABLE IF EXISTS `erp_student_medical`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_medical` (
  `medical_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `blood_group` enum('AB_MINUS','AB_PLUS','A_MINUS','A_PLUS','B_MINUS','B_PLUS','O_MINUS','O_PLUS','UNKNOWN') NOT NULL,
  `height_cm` decimal(5,2) DEFAULT NULL,
  `weight_kg` decimal(5,2) DEFAULT NULL,
  `allergies` varchar(500) DEFAULT NULL,
  `chronic_conditions` varchar(500) DEFAULT NULL,
  `ongoing_medication` varchar(500) DEFAULT NULL,
  `special_needs` varchar(500) DEFAULT NULL,
  `fit_for_sports` tinyint(1) NOT NULL DEFAULT 1,
  `emergency_doctor_name` varchar(150) DEFAULT NULL,
  `emergency_doctor_mobile` varchar(20) DEFAULT NULL,
  `preferred_hospital` varchar(150) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`medical_id`),
  UNIQUE KEY `uk_student_medical` (`student_id`),
  KEY `idx_medical_branch` (`branch_id`),
  KEY `idx_medical_admission` (`admission_no`),
  KEY `idx_medical_blood_group` (`blood_group`),
  KEY `idx_medical_sports` (`fit_for_sports`),
  CONSTRAINT `fk_medical_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_medical_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_medical_height` CHECK (`height_cm` is null or `height_cm` > 0),
  CONSTRAINT `chk_medical_weight` CHECK (`weight_kg` is null or `weight_kg` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_medical`
--

LOCK TABLES `erp_student_medical` WRITE;
/*!40000 ALTER TABLE `erp_student_medical` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_medical` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_student_transport`
--

DROP TABLE IF EXISTS `erp_student_transport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_transport` (
  `transport_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` bigint(20) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `route_id` bigint(20) NOT NULL,
  `vehicle_id` bigint(20) DEFAULT NULL,
  `pickup_point_id` bigint(20) DEFAULT NULL,
  `transport_start_date` date NOT NULL,
  `transport_end_date` date DEFAULT NULL,
  `monthly_fee` decimal(12,2) NOT NULL DEFAULT 0.00,
  `annual_fee` decimal(12,2) NOT NULL DEFAULT 0.00,
  `discount_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `payable_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `transport_status` enum('ACTIVE','CANCELLED','COMPLETED','INACTIVE','SUSPENDED') NOT NULL,
  `payment_status` enum('PAID','PARTIAL','PENDING') NOT NULL,
  `seat_number` varchar(20) DEFAULT NULL,
  `emergency_contact` varchar(100) DEFAULT NULL,
  `emergency_mobile` varchar(20) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`transport_id`),
  UNIQUE KEY `uk_student_transport` (`student_id`,`academic_year`),
  KEY `idx_transport_student` (`student_id`),
  KEY `idx_transport_branch` (`branch_id`),
  KEY `idx_transport_route` (`route_id`),
  KEY `idx_transport_vehicle` (`vehicle_id`),
  KEY `idx_transport_pickup` (`pickup_point_id`),
  KEY `idx_transport_status` (`transport_status`),
  KEY `idx_transport_payment` (`payment_status`),
  KEY `idx_transport_year` (`academic_year`),
  KEY `idx_transport_admission` (`admission_no`),
  CONSTRAINT `fk_transport_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE,
  CONSTRAINT `fk_transport_student` FOREIGN KEY (`student_id`) REFERENCES `erp_students` (`student_id`) ON UPDATE CASCADE,
  CONSTRAINT `chk_monthly_fee` CHECK (`monthly_fee` >= 0),
  CONSTRAINT `chk_annual_fee` CHECK (`annual_fee` >= 0),
  CONSTRAINT `chk_discount` CHECK (`discount_amount` >= 0),
  CONSTRAINT `chk_payable` CHECK (`payable_amount` >= 0),
  CONSTRAINT `chk_transport_dates` CHECK (`transport_end_date` is null or `transport_end_date` >= `transport_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_transport`
--

LOCK TABLES `erp_student_transport` WRITE;
/*!40000 ALTER TABLE `erp_student_transport` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_student_transport` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_students`
--

DROP TABLE IF EXISTS `erp_students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_students` (
  `student_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `application_id` bigint(20) DEFAULT NULL,
  `branch_id` int(11) NOT NULL,
  `admission_no` varchar(50) NOT NULL,
  `learner_lin` varchar(50) DEFAULT NULL,
  `admission_year` int(11) NOT NULL,
  `student_code` varchar(50) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `middle_name` varchar(100) DEFAULT NULL,
  `last_name` varchar(100) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `nationality` varchar(100) DEFAULT NULL,
  `blood_group_id` bigint(20) DEFAULT NULL,
  `religion_id` bigint(20) DEFAULT NULL,
  `category_id` bigint(20) DEFAULT NULL,
  `house_no` varchar(50) DEFAULT NULL,
  `street` varchar(150) DEFAULT NULL,
  `village` varchar(100) DEFAULT NULL,
  `town_city` varchar(100) DEFAULT NULL,
  `district` varchar(100) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  `country` varchar(100) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `photo_path` varchar(255) DEFAULT NULL,
  `student_status` varchar(30) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `admission_no` (`admission_no`),
  UNIQUE KEY `student_code` (`student_code`),
  KEY `fk_students_application` (`application_id`),
  KEY `fk_students_branch` (`branch_id`),
  CONSTRAINT `fk_students_application` FOREIGN KEY (`application_id`) REFERENCES `erp_applications` (`application_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_students_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_students`
--

LOCK TABLES `erp_students` WRITE;
/*!40000 ALTER TABLE `erp_students` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_students` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_subjects`
--

DROP TABLE IF EXISTS `erp_subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_subjects` (
  `subject_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `subject_code` varchar(20) NOT NULL,
  `subject_name` varchar(100) NOT NULL,
  `subject_short_name` varchar(50) DEFAULT NULL,
  `subject_type` varchar(30) NOT NULL DEFAULT 'CORE',
  `is_practical` tinyint(1) NOT NULL DEFAULT 0,
  `display_order` int(11) NOT NULL DEFAULT 1,
  `description` varchar(500) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `uk_subject_code` (`subject_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_subjects`
--

LOCK TABLES `erp_subjects` WRITE;
/*!40000 ALTER TABLE `erp_subjects` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_subjects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_tasks`
--

DROP TABLE IF EXISTS `erp_tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_tasks` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `module` varchar(30) NOT NULL DEFAULT 'ADMISSION',
  `reference_type` varchar(30) NOT NULL,
  `reference_id` bigint(20) NOT NULL,
  `task_type` varchar(100) NOT NULL,
  `action_code` varchar(100) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `assigned_to` int(11) DEFAULT NULL,
  `assigned_role` varchar(50) DEFAULT NULL,
  `assigned_at` datetime DEFAULT current_timestamp(),
  `accepted_at` datetime DEFAULT NULL,
  `status` enum('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `priority` enum('LOW','NORMAL','HIGH','URGENT') NOT NULL DEFAULT 'NORMAL',
  `due_date` datetime DEFAULT NULL,
  `started_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `completed_by` int(11) DEFAULT NULL,
  `parent_task_id` bigint(20) DEFAULT NULL,
  `sequence_no` int(11) NOT NULL DEFAULT 0,
  `branch_id` int(11) DEFAULT NULL,
  `school_id` int(11) DEFAULT NULL,
  `remarks` text DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `version` int(11) NOT NULL DEFAULT 0,
  `created_by` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`task_id`),
  KEY `idx_task_reference` (`reference_type`,`reference_id`),
  KEY `idx_task_assigned` (`assigned_to`,`status`),
  KEY `idx_task_module` (`module`,`status`),
  KEY `idx_task_due` (`status`,`due_date`),
  KEY `idx_task_branch` (`branch_id`,`status`),
  KEY `idx_task_parent` (`parent_task_id`),
  KEY `fk_task_completed_user` (`completed_by`),
  CONSTRAINT `fk_task_assigned_user` FOREIGN KEY (`assigned_to`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_task_completed_user` FOREIGN KEY (`completed_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_task_parent` FOREIGN KEY (`parent_task_id`) REFERENCES `erp_tasks` (`task_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_tasks`
--

LOCK TABLES `erp_tasks` WRITE;
/*!40000 ALTER TABLE `erp_tasks` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_user_roles`
--

DROP TABLE IF EXISTS `erp_user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_user_roles` (
  `user_role_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`user_role_id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `fk_userrole_role` (`role_id`),
  CONSTRAINT `fk_userrole_role` FOREIGN KEY (`role_id`) REFERENCES `erp_roles` (`role_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_userrole_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_user_roles`
--

LOCK TABLES `erp_user_roles` WRITE;
/*!40000 ALTER TABLE `erp_user_roles` DISABLE KEYS */;
INSERT INTO `erp_user_roles` VALUES (1,1,1,1,0,NULL,'2026-07-08 04:27:08',NULL,'2026-07-08 04:27:08'),(2,2,3,1,0,NULL,'2026-07-08 04:27:08',NULL,'2026-07-08 04:27:08'),(3,3,3,1,0,NULL,'2026-07-08 04:27:08',NULL,'2026-07-08 04:27:08'),(4,4,3,1,0,NULL,'2026-07-08 04:27:08',NULL,'2026-07-08 04:27:08'),(5,5,3,1,0,NULL,'2026-07-09 11:55:07',NULL,'2026-07-09 11:55:07'),(6,6,3,1,0,NULL,'2026-07-09 12:21:33',NULL,'2026-07-09 12:21:33'),(7,7,3,1,0,NULL,'2026-07-10 06:03:59',NULL,'2026-07-10 06:03:59'),(8,26,3,1,0,NULL,'2026-07-20 21:33:40',NULL,'2026-07-20 21:33:40');
/*!40000 ALTER TABLE `erp_user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_user_sessions`
--

DROP TABLE IF EXISTS `erp_user_sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_user_sessions` (
  `session_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `session_token` varchar(255) NOT NULL,
  `login_time` datetime NOT NULL,
  `last_activity_time` datetime NOT NULL,
  `expiry_time` datetime NOT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `device_name` varchar(255) DEFAULT NULL,
  `browser` varchar(150) DEFAULT NULL,
  `operating_system` varchar(150) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  `version` bigint(20) NOT NULL DEFAULT 0,
  `created_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` bigint(20) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`session_id`),
  UNIQUE KEY `uk_session_token` (`session_token`),
  KEY `fk_usersession_user` (`user_id`),
  KEY `idx_usersession_expiry` (`expiry_time`),
  KEY `idx_usersession_active` (`active`),
  CONSTRAINT `fk_usersession_user` FOREIGN KEY (`user_id`) REFERENCES `erp_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_user_sessions`
--

LOCK TABLES `erp_user_sessions` WRITE;
/*!40000 ALTER TABLE `erp_user_sessions` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_user_sessions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `erp_users`
--

DROP TABLE IF EXISTS `erp_users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT NULL,
  `assigned_branch` int(11) DEFAULT NULL,
  `is_active` int(11) DEFAULT 1,
  `must_change_password` tinyint(1) NOT NULL DEFAULT 0,
  `temporary_password_created_at` datetime DEFAULT NULL,
  `temporary_password_expires_at` datetime DEFAULT NULL,
  `password_changed_at` datetime DEFAULT NULL,
  `credential_delivery_status` varchar(30) NOT NULL DEFAULT 'NOT_REQUIRED',
  `credentials_sent_at` datetime DEFAULT NULL,
  `credential_delivery_attempts` int(11) NOT NULL DEFAULT 0,
  `credential_version` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `updated_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `assigned_branch` (`assigned_branch`),
  KEY `idx_erp_users_temp_credentials` (`must_change_password`,`temporary_password_expires_at`),
  CONSTRAINT `erp_users_ibfk_1` FOREIGN KEY (`assigned_branch`) REFERENCES `erp_branches` (`branch_id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_users`
--

LOCK TABLES `erp_users` WRITE;
/*!40000 ALTER TABLE `erp_users` DISABLE KEYS */;
INSERT INTO `erp_users` VALUES (1,'erpsadmin','$2a$10$95vGGFy8A47oEfW0F.DDe.mlC4LceWJXhFeXBRWp7ECXnEPPotct6','SUPER_ADMIN',NULL,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-06-25 08:42:22.000000',NULL,'2026-06-25 08:42:22.000000',NULL),(2,'u011@montfort.ug','$2a$10$1Kxi4QIxq7np1CycnakyLeK5SuH44cgkRUL1ZG2FyiPopJ02RfNiK','BRANCH_ADMIN',1,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-06-25 08:43:11.000000',NULL,'2026-06-25 08:43:11.000000',NULL),(3,'u021@montfort.ug','$2a$10$ltJEDZUSj9WL5o3PYLB9k.8xJYUtg2sGBwNTDg/RTQYYnrYDyy6/C','BRANCH_ADMIN',2,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-06-25 08:45:46.000000',NULL,'2026-06-25 08:45:46.000000',NULL),(4,'u031@montfort.ug','$2a$10$YYFEuPbY2quMsZAaWyFJneR5I0gdwXoCKXyjufpe5jRq59l1h.zDK','BRANCH_ADMIN',3,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-06-25 08:46:46.000000',NULL,'2026-06-25 08:46:46.000000',NULL),(5,'u032@montfort.ug','$2a$10$TEdW7h0DetzBiBd8qEZYguzT4JKZm4O7Y3ZqX/K7yki7YqK4vZfmy','BRANCH_ADMIN',4,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-07-09 11:55:07.000000','1','2026-07-09 11:55:07.000000','1'),(6,'u033@montfort.ug','$2a$10$DsuroF.3J2bJuzdsdTrVT.15V4p3yYS3UUHiHdZ0aK5cTPU/kSoAK','BRANCH_ADMIN',8,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-07-09 12:21:32.000000','1','2026-07-09 12:21:32.000000','1'),(7,'u034@montfort.ug','$2a$10$fpVQObHpAh.RPhtcX6LLluJ3FD03dlys5r0eQ0r6YAbw3sRjrcnm2','BRANCH_ADMIN',9,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-07-10 06:03:59.000000','1','2026-07-10 06:03:59.000000','1'),(17,'U011-T-26-001','$2a$10$Oy1PamBprCdCP5Pm2hUPo.b5R1WoDiBL5nxaTXGIuwU0euqC6NbR2','EMPLOYEE',1,1,0,NULL,NULL,NULL,'NOT_REQUIRED',NULL,0,0,'2026-07-18 18:22:41.000000','2','2026-07-18 18:22:41.000000','2'),(26,'u02322@montfort.ug','$2a$10$1fVO5PI7fSf1gwKyavhEwODWaTKwd00KglAkrUz/CAGfGmq.Cm/Ve','BRANCH_ADMIN',13,1,0,NULL,NULL,'2026-07-20 16:28:15','ACCEPTED',NULL,0,2,'2026-07-20 21:33:40.000000','1','2026-07-20 21:58:15.000000','system');
/*!40000 ALTER TABLE `erp_users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-21 12:11:11
