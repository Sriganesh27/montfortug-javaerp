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
  `created_by` varchar(100) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`department_id`),
  UNIQUE KEY `uk_branch_dept_code` (`branch_id`,`department_code`),
  CONSTRAINT `fk_department_branch` FOREIGN KEY (`branch_id`) REFERENCES `erp_branches` (`branch_id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_departments`
--

LOCK TABLES `erp_departments` WRITE;
/*!40000 ALTER TABLE `erp_departments` DISABLE KEYS */;
INSERT INTO `erp_departments` VALUES (1,1,'ADMIN','Administration',0,'School administration and management','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(2,1,'ACADEMICS','Academics',1,'Teaching and academic activities','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(3,1,'ADMISSIONS','Admissions',0,'Admissions and student enrollment','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(4,1,'EXAMINATIONS','Examinations',1,'Examinations and assessments','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(5,1,'FINANCE','Finance',0,'Accounts and school finance','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(6,1,'HR','Human Resources',0,'Human resource management','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(7,1,'ICT','ICT',0,'Information Technology services','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(8,1,'LIBRARY','Library',1,'Library management','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(9,1,'DISCIPLINE','Discipline',1,'Student discipline and welfare','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(10,1,'COUNSELLING','Guidance & Counselling',1,'Student guidance and counselling','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(11,1,'HEALTH','Health Unit',0,'School clinic and medical services','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(12,1,'TRANSPORT','Transport',0,'School transport management','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(13,1,'HOSTEL','Hostel',0,'Boarding and hostel management','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(14,1,'SPORTS','Sports',1,'Sports and co-curricular activities','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(15,1,'PROCUREMENT','Procurement',0,'Purchasing and procurement','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(16,1,'MAINTENANCE','Maintenance',0,'Maintenance of school facilities','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(17,1,'SECURITY','Security',0,'School security','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(18,1,'ESTATE','Estate',0,'Estate and infrastructure management','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(19,1,'STORE','Store',0,'Stores and inventory management','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(20,1,'PUBLIC_RELATIONS','Public Relations',0,'Public relations and communication','ACTIVE',1,0,'1','2026-07-10 11:13:53','1','2026-07-10 11:13:53'),(21,1,'DEP_MATH','MATHS',1,'HELLO','ACTIVE',1,0,'1','2026-07-11 17:06:21','1','2026-07-11 17:06:21'),(22,1,'DEPYNJ','nnnn',1,'','ACTIVE',1,0,'1','2026-07-11 18:24:12','1','2026-07-11 18:24:12');
/*!40000 ALTER TABLE `erp_departments` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:46:57
