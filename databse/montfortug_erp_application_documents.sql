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
  KEY `fk_application_document_application` (`application_id`),
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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:46:30
