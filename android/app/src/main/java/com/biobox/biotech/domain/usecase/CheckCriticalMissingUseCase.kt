package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Material

class CheckCriticalMissingUseCase {
    
    operator fun invoke(materials: List<Pair<Material, Int>>): Boolean {
        return materials.any { (material, foundQty) ->
            material.stockMin > 0.0 && foundQty < material.cantidadRequerida
        }
    }
}
