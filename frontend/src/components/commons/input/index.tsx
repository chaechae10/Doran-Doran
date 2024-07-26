import style from "./index.module.css";

export default function Input({
  type = "text",
  placeholder = "label",
  value = "",
  name,
  setValue = () => {},
}: {
  type?: "text" | "password" | "email";
  placeholder?: string;
  value?: string | number;
  name? : string;
  setValue?:
    | React.Dispatch<React.SetStateAction<string | number>>
    | ((value: string) => void);
}) {
  return (
    <input
      value={value}
      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
        setValue(e.target.value);
      }}
      type={type}
      className={`${style.input} ${name && style[name]}`}
      placeholder={placeholder}
    />
  );
}
