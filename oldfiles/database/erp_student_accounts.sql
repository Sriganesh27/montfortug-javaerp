-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 68.178.237.26    Database: montfortug
-- ------------------------------------------------------
-- Server version	5.5.5-10.11.16-MariaDB-cll-lve

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
-- Table structure for table `erp_student_accounts`
--

DROP TABLE IF EXISTS `erp_student_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `erp_student_accounts` (
  `account_id` int(11) NOT NULL AUTO_INCREMENT,
  `AdmissionNo` int(11) NOT NULL,
  `branch_id` int(11) NOT NULL,
  `username` varchar(100) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `is_active` int(11) DEFAULT 1,
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `username` (`username`),
  KEY `branch_id` (`branch_id`,`AdmissionNo`),
  CONSTRAINT `erp_student_accounts_ibfk_1` FOREIGN KEY (`branch_id`, `AdmissionNo`) REFERENCES `erp_students` (`branch_id`, `AdmissionNo`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `erp_student_accounts`
--

LOCK TABLES `erp_student_accounts` WRITE;
/*!40000 ALTER TABLE `erp_student_accounts` DISABLE KEYS */;
INSERT INTO `erp_student_accounts` VALUES (1,1,1,'U011-26-P2-0001','$2a$10$7ID6bdb5CnCTIwtazyCtY.9JF2YZSm9RHSZ9.jgOZPUqjH7cynqH2',1),(2,2,1,'U011-26-P2-0002','$2a$10$VYJ/ryyJbpC/1erZYdehOeOEmTK2LW8.oRqNINC0H6mX9Rp9Yj/Ea',1),(3,3,1,'U011-26-P2-0003','$2a$10$VEpfwN9g389FES6p/aFou.KYdmf/soj/MHBJdJy21wOpbgx8x1/P6',1),(4,4,1,'U011-26-P2-0004','$2a$10$RMciHoLCCtPoAOsLYIqU1.g/lmSMxaVkVwdax1SXByALdHA/4hFMG',1),(5,5,1,'U011-26-P2-0005','$2a$10$u6MhrEdP7mZNY8hMJa8fLebWwoPOQtfquDRVS8TLpw3sjnuPu.H32',1),(6,6,1,'U011-26-P2-0006','$2a$10$K1CoiKOXCLjTtRocHJlqdu8AMvHld/0QpSfOwtWZ445UZEORdANQi',1),(9,7,1,'U011-26-P2-0007','$2a$10$KWkrmFPP79SlR9p/KqQJfehsAha5lH6e681bs3y6zgk/UWqWobDXm',1),(10,8,1,'U011-26-P2-0008','$2a$10$.1fucXmtWTgZF.j8jkw.2O3c8hOqp1fJaKw9IjBHYzrR3A766lete',1),(11,9,1,'U011-26-N2-0009','$2a$10$qaQh5ChYlCSUa2PwZPW1LupVJnRaqMn4/LW5eUZNwumSTLErx9aSu',1),(12,10,1,'U011-26-P2-0010','$2a$10$c6b9.HFpFY14BUxryR2EbOvboFSg1BcSEjXFZEyoM4iImhJ3U8nHa',1);
/*!40000 ALTER TABLE `erp_student_accounts` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-09 19:47:20
