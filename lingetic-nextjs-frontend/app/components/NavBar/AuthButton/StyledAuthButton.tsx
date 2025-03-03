import { ButtonHTMLAttributes } from "react";

export default function StyledAuthButton(
  props: ButtonHTMLAttributes<HTMLButtonElement>
) {
  return (
    <button
      {...props}
      className="bg-skin-button-primary text-skin-inverted px-4 py-2 rounded-md hover:bg-skin-button-primary-hover"
    />
  );
}
