export interface ButtonProps {
  value: string;
}

export function Button({ value }: ButtonProps) {
  return <button>{value}</button>;
}

export default Button;
