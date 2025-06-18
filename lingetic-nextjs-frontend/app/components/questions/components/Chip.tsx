interface ChipProps {
  text: string;
}

export default function Chip({ text }: ChipProps) {
  return (
    <span className="inline-block bg-skin-fill-accent text-skin-base border border-skin-border rounded-full px-2 py-0.5 text-xs">
      {text}
    </span>
  );
}
