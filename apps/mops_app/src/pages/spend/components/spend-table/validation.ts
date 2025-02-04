import { LineItem, LineItemErrors } from "./types";
import { useState } from "react";

export const NAME_MAX_LENGTH = 256;

export const NAME_REQUIRED_ERROR = "Name is Required";
export const NAME_MAX_LENGTH_ERROR = "Max length is " + NAME_MAX_LENGTH;

export function validateLineItem(lineItem: LineItem): LineItemErrors {
  return {
    name: !validateRequired(lineItem.name)
      ? NAME_REQUIRED_ERROR
      : !validateLength(lineItem.name, 256)
        ? NAME_MAX_LENGTH_ERROR
        : "",
  };
}

function validateRequired(value: string): boolean {
  return !!value.length;
}

function validateLength(value: string, maxLength: number): boolean {
  return value.length <= maxLength;
}

export function useLineItemValidation() {
  const [validationErrors, setValidationErrors] = useState<LineItemErrors>({});

  const removeErrorsFor = (key: keyof LineItemErrors) => {
    return () =>
      setValidationErrors({
        ...validationErrors,
        [key]: undefined,
      });
  };

  const clearErrors = () => {
    setValidationErrors({});
  };

  return {
    validationErrors,
    setValidationErrors,
    removeErrorsFor,
    clearErrors,
  };
}
