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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_applications`
--

LOCK TABLES `erp_applications` WRITE;
/*!40000 ALTER TABLE `erp_applications` DISABLE KEYS */;
INSERT INTO `erp_applications` VALUES (1,'APP-2026-U011-001',1,2026,6,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Sibling','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-001/photo_1782899073071_aa106764.jpeg','',NULL,NULL,NULL,'2026-07-01 09:44:32','2026-07-01 09:44:32',1),(2,'APP-2026-U011-002',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Sibling','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-002/photo_1782899658052_258e0834.jpeg','',NULL,NULL,NULL,'2026-07-01 09:54:17','2026-07-01 09:54:17',1),(3,'APP-2026-U031-001',3,2026,12,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-001/photo_1782900179290_a1efd19b.jpeg','',NULL,NULL,NULL,'2026-07-01 10:02:58','2026-07-01 10:02:58',1),(4,'APP-2026-U021-001',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','','','koo','koo','','kkkk','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Step-father','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-001/photo_1782900439677_98a61d45.jpeg','',NULL,NULL,NULL,'2026-07-01 10:07:18','2026-07-01 10:07:18',1),(5,'APP-2026-U011-003',1,2026,6,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-03-04','Uganda','2026-07-01','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-003/doc_1782901940666_2579778c.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-003/photo_1782901940649_7964978a.jpeg','',NULL,NULL,NULL,'2026-07-01 10:32:19','2026-07-01 10:32:19',1),(6,'APP-2026-U031-002',3,2026,11,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','FEMALE','2003-07-02','Uganda','2026-07-01','','koo','koo','','kkkk','p',44,'',NULL,'','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-002/doc_1782907247101_64b18ed3.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','d','ho','j','5','5588','SUBMITTED','/uploads/applications/U031-Pere Achte Secondary _ Senior Secondary School,Isunga/APP-2026-U031-002/photo_1782907247078_26d0e9c4.jpeg','',NULL,NULL,NULL,'2026-07-01 12:00:45','2026-07-01 12:00:45',1),(7,'APP-2026-U011-004',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2003-07-02','Uganda','2026-07-01','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-004/doc_1782908680956_0f8b26f7.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','d','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-004/photo_1782908680940_e24faa98.jpeg','',NULL,NULL,NULL,'2026-07-01 12:24:40','2026-07-01 12:24:40',1),(8,'APP-2026-U011-005',1,2026,6,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2003-07-02','Uganda','2026-07-01','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-005/doc_1782909458395_1f0a7a1f.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Brother','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-005/photo_1782909458376_86e95df1.jpeg','',NULL,NULL,NULL,'2026-07-01 12:37:37','2026-07-01 12:37:37',1),(9,'APP-2026-U021-002',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2002-01-01','Uganda','2026-07-01','','koo','koo','','','',NULL,'',NULL,'','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-002/doc_1782912293672_bb65484d.pdf;','Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','','','','',0,'central','d','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-002/photo_1782912293668_5a468c1d.jpeg','',NULL,NULL,NULL,'2026-07-01 13:24:50','2026-07-01 13:24:50',1),(10,'APP-2026-U021-003',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-06-30','Uganda','2026-07-02','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-003/doc_1782978897130_86c418c5.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Aunt','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-003/photo_1782978897130_50f0bd55.jpeg','',NULL,NULL,NULL,'2026-07-02 07:54:55','2026-07-02 07:54:55',1),(11,'APP-2026-U021-004',2,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2026-06-29','Uganda','2026-07-02','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-004/doc_1782979827600_b3d1f4d9.pdf;','Sri Ganesh Sudhagani','5555','sriganeshgoud9154@gmail.com','44','55',55,'Sri Ganesh Sudhagani','55646','sriganeshgoud9154@gmail.com','5','555',556,'Sri Ganesh Sudhagani','555','','sriganeshgoud9154@gmail.com','Aunt','55','55','kk',55,'central','gg','ho','j','5','5588','SUBMITTED','/uploads/applications/U021-St.Montfort Nursery _ Primary School,Mpala/APP-2026-U021-004/photo_1782979827572_c1d518d2.jpeg','',NULL,NULL,NULL,'2026-07-02 08:10:26','2026-07-02 08:10:26',1),(12,'APP-2026-U011-006',1,2026,1,'','NEW',NULL,0,'jai@gmail','+256786844243','jai','','mana','MALE','2024-08-14','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','','','','','','',0,'','','','','',0,'','','','','Grandparent','','','',0,'','Wakisa','','','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-02 09:24:02','2026-07-02 09:24:02',1),(13,'APP-2026-U011-007',1,2026,1,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+919154763356','SRI','GANESH','SUDHAGANI','MALE','2002-01-01','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','','','','',0,'central','gg','ho','j','5','5588','SUBMITTED','','',NULL,NULL,NULL,'2026-07-02 09:24:11','2026-07-02 09:24:11',1),(14,'APP-2026-U011-008',1,2026,3,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 88','Shiva','GANESH','Kurada','MALE','2002-01-01','Uganda','2026-07-02','','koo','koo','','kkkk','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/doc_1783005355009_ca91e0bd.pdf;/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/doc_1783005355011_01b40eef.pdf;','Sri Ganesh Sudhagani','+256 6','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Brother','','','',0,'','gg','','Miyapur','','','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-008/photo_1783005354997_3f41ce74.jpeg','',NULL,NULL,NULL,'2026-07-02 15:15:53','2026-07-02 15:15:53',1),(15,'APP-2026-U011-009',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 123','Shiva','','Kurada','MALE','2022-11-28','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-009/doc_1783006785967_3fe38833.pdf;','Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Grandparent','','','',0,'central','gg','ho','Miyapur','5','5588','SUBMITTED','/uploads/applications/U011-St. Kizito Nursery _ Primary School,Kyebando/APP-2026-U011-009/photo_1783006785964_dae4189c.jpeg','',NULL,NULL,NULL,'2026-07-02 15:39:42','2026-07-02 15:39:42',1),(16,'APP-2026-U011-010',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 79','Sudhagani','','Ganesh','MALE','2022-12-01','Uganda','2026-07-02','','','','','','',NULL,'',NULL,'','','Hh','','','','',0,'Nv','','','','',0,'','','','','','','','',0,'','G','','','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-02 17:28:33','2026-07-02 17:28:33',1),(17,'APP-2026-U011-011',1,2026,2,'','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 90','Shiva','GANESH','Kurada','MALE','2002-01-01','Uganda','2026-07-03','','koo','koo','','','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','+256 6','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Brother','','','',0,'','gg','','Miyapur','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-03 09:58:26','2026-07-03 09:58:26',1),(18,'APP-2026-U011-012',1,2026,1,'Term II','NEW',NULL,0,'sriganeshgoud9154@gmail.com','+256 90','Shiva','GANESH','Kurada','MALE','2002-01-01','Uganda','2026-07-03','','koo','koo','','','',NULL,'',NULL,'','','Sri Ganesh Sudhagani','+256 6','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','sriganeshgoud9154@gmail.com','','',0,'Sri Ganesh Sudhagani','','','sriganeshgoud9154@gmail.com','Aunt','','','',0,'','gg','','Miyapur','','','SUBMITTED','','',NULL,NULL,NULL,'2026-07-03 10:27:24','2026-07-03 10:27:24',1);
/*!40000 ALTER TABLE `erp_applications` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-04 14:12:06
