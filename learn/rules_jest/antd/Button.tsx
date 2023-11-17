import { Button as AButton } from "antd";

export interface ButtonProps {
  value: string;
}

export function Button({ value }: ButtonProps) {
  return <AButton type="primary">{value}</AButton>;
}

export default Button;
