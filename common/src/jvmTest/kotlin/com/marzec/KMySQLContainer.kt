package com.marzec

import org.testcontainers.containers.MySQLContainer

class KMySQLContainer(image: String) : MySQLContainer<KMySQLContainer>(image)