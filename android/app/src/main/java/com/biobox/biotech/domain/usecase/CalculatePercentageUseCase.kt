package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Material
import kotlin.math.min

class CalculatePercentageUseCase {
    
    operator fun invoke(materials: List<Pair<Material, Int>>): Int {
        var totalRequired = 0
        var totalFound = 0
        
        materials.forEach { (material, foundQty) ->
            if (material.cantidadRequerida > 0) {
                totalRequired += material.cantidadRequerida
                // Count found quantity up to required quantity
                totalFound += min(foundQty, material.cantidadRequerida)
            }
        }
        
        if (totalRequired == 0) return 0
        
        return ((totalFound.toDouble() / totalRequired.toDouble()) * 100).toInt().coerceIn(0, 100)
    }
}
