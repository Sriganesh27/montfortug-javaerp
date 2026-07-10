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
  `transaction_type` enum('FEE_ASSIGNED','PAYMENT','PARTIAL_PAYMENT','SCHOLARSHIP','CONCESSION','DISCOUNT','FINE','WAIVER','REFUND','REVERSAL','ADJUSTMENT') NOT NULL,
  `payment_mode` enum('CASH','CHEQUE','BANK_TRANSFER','MOBILE_MONEY','CREDIT_CARD','DEBIT_CARD','ONLINE','SCHOLARSHIP','WAIVER') DEFAULT NULL,
  `transaction_reference` varchar(150) DEFAULT NULL,
  `transaction_date_time` datetime NOT NULL,
  `debit_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `credit_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `running_balance` decimal(12,2) NOT NULL,
  `currency` char(3) NOT NULL DEFAULT 'UGX',
  `ledger_status` enum('ACTIVE','CANCELLED','REVERSED') NOT NULL DEFAULT 'ACTIVE',
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
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-11  0:29:35
