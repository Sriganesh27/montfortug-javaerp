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
-- Table structure for table `erp_employee_contacts`
--

DROP TABLE IF EXISTS `erp_employee_contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_employee_contacts` (
  `employee_contact_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `employee_id` bigint(20) NOT NULL,
  `employee_contact_name` varchar(255) NOT NULL,
  `employee_contact_relationship` enum('FATHER','MOTHER','SPOUSE','SON','DAUGHTER','BROTHER','SISTER','GUARDIAN','RELATIVE','FRIEND','MANAGER','REFERENCE','OTHER') NOT NULL,
  `employee_contact_type` enum('EMERGENCY','NEXT_OF_KIN','REFERENCE','GUARDIAN','OTHER') NOT NULL DEFAULT 'EMERGENCY',
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
  `created_by` int(11) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_by` int(11) DEFAULT NULL,
  `updated_at` datetime NOT NULL,
  `version` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`employee_contact_id`),
  KEY `fk_empcontact_employee` (`employee_id`),
  KEY `fk_empcontact_createdby` (`created_by`),
  KEY `fk_empcontact_updatedby` (`updated_by`),
  CONSTRAINT `fk_empcontact_createdby` FOREIGN KEY (`created_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_empcontact_employee` FOREIGN KEY (`employee_id`) REFERENCES `erp_employees` (`employee_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_empcontact_updatedby` FOREIGN KEY (`updated_by`) REFERENCES `erp_users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_employee_contacts`
--

LOCK TABLES `erp_employee_contacts` WRITE;
/*!40000 ALTER TABLE `erp_employee_contacts` DISABLE KEYS */;
/*!40000 ALTER TABLE `erp_employee_contacts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:46:37
