package com.wineadvisor.wineadvisor.DTO.wines;


import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewTasteDTO {
    @Valid
    private NewStructureDTO structure;
}
