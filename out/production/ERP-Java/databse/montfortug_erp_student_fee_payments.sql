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
  `payment_mode` enum('CASH','CHEQUE','BANK_TRANSFER','MOBILE_MONEY','CREDIT_CARD','DEBIT_CARD','ONLINE','SCHOLARSHIP','WAIVER') NOT NULL,
  `transaction_reference` varchar(150) DEFAULT NULL,
  `collection_point` varchar(100) DEFAULT NULL,
  `payment_status` enum('PENDING','SUCCESS','FAILED','CANCELLED','REVERSED','REFUNDED') NOT NULL DEFAULT 'SUCCESS',
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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-14 12:47:03
