package com.alumni.linkedinsearcher.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlumniSearchRequest {

    @NotBlank(message = "university must not be blank")
    private String university;

    @NotBlank(message = "designation must not be blank")
    private String designation;

    @Min(value = 1950, message = "passoutYear must be a realistic year")
    @Max(value = 2100, message = "passoutYear must be a realistic year")
    private Integer passoutYear;

    public boolean hasPassoutYear() {
        return passoutYear != null;
    }
}
