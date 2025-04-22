package com.wineadvisor.wineadvisor.DTO.WineDTO;

import org.springframework.data.mongodb.core.mapping.Field;

import com.wineadvisor.wineadvisor.model.fields.wines.Style;
import com.wineadvisor.wineadvisor.model.fields.wines.Taste;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWineDTO {
    private Long wineId;

    private String name;
    private String type;

    @Field("is_natural")
    private Boolean isNatural;

    private Taste taste;
    private Style style;
}
