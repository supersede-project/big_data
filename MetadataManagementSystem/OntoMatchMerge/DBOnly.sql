-- phpMyAdmin SQL Dump
-- version 4.4.13.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Mar 23, 2016 at 08:53 AM
-- Server version: 10.0.23-MariaDB-0ubuntu0.15.10.1
-- PHP Version: 5.6.11-1ubuntu3.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `DBOnly`
--
CREATE DATABASE IF NOT EXISTS `DBOnly` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `DBOnly`;

-- --------------------------------------------------------

--
-- Table structure for table `Items`
--

DROP TABLE IF EXISTS `Items`;
CREATE TABLE IF NOT EXISTS `Items` (
  `ItemID` int(11) NOT NULL,
  `Name` varchar(50) NOT NULL,
  `Price` int(11) NOT NULL,
  `Percentage` decimal(3,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `Items`
--

INSERT INTO `Items` (`ItemID`, `Name`, `Price`, `Percentage`) VALUES
(1, 'pretium nisl', 92012, '0.95'),
(2, 'volutpat quam', 57268, '0.70'),
(3, 'nullam orci pede venenatis', 52916, '0.46'),
(4, 'posuere cubilia curae donec pharetra', 46925, '0.47'),
(5, 'sit', 10839, '0.35'),
(6, 'sodales scelerisque mauris sit', 56555, '0.12'),
(7, 'nibh ligula nec', 58984, '0.48'),
(8, 'morbi vestibulum velit', 77434, '0.08'),
(9, 'odio porttitor id consequat', 84275, '0.20'),
(10, 'a feugiat et', 60252, '0.89'),
(11, 'odio in hac', 62720, '0.20'),
(12, 'nulla elit ac', 42813, '0.68'),
(13, 'nulla suspendisse', 89768, '0.11'),
(14, 'quis turpis sed', 41991, '0.12'),
(15, 'at nunc commodo placerat praesent', 21349, '0.46'),
(16, 'tempus sit amet sem', 66604, '0.30'),
(17, 'eget tempus', 56844, '0.23'),
(18, 'venenatis', 88591, '0.27'),
(19, 'in', 59230, '0.74'),
(20, 'sapien', 66989, '0.85'),
(21, 'feugiat et eros', 10274, '0.85'),
(22, 'mollis molestie lorem quisque', 62709, '0.12'),
(23, 'nibh', 36035, '0.01'),
(24, 'massa donec', 90366, '0.62'),
(25, 'nascetur', 13847, '0.79'),
(26, 'vitae mattis nibh ligula', 35731, '0.58'),
(27, 'risus praesent lectus vestibulum', 25159, '1.00'),
(28, 'sit amet', 46347, '0.27'),
(29, 'elementum pellentesque quisque porta', 19838, '0.81'),
(30, 'rutrum rutrum neque aenean', 28466, '0.73');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Items`
--
ALTER TABLE `Items`
  ADD PRIMARY KEY (`ItemID`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
