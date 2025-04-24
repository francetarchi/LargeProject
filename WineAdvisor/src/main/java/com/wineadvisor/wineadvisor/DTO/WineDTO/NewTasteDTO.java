package com.wineadvisor.wineadvisor.DTO.WineDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewTasteDTO {
    @Valid
    @JsonProperty("structure")
    private NewStructureDTO structure;
}
