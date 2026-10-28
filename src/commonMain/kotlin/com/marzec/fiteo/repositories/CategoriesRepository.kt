package com.marzec.fiteo.repositories

import com.marzec.core.repository.CommonRepository
import com.marzec.fiteo.model.domain.Category
import com.marzec.fiteo.model.domain.UpdateCategory

interface CategoriesRepository : CommonRepository<String, Category, Category, UpdateCategory>
