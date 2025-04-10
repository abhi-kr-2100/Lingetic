import assert from "@/utilities/assert";
import Image from "next/image";
import Link from "next/link";

interface LanguageCardProps {
  id: string;
  name: string;
  image: string;
}

export default function LanguagePlayCard({
  id,
  name,
  image,
}: LanguageCardProps) {
  validatePropsOrDie({ id, name, image });

  return (
    <div className="shadow-lg rounded-lg overflow-hidden">
      <Image
        src={image ?? "/img/languages/placeholder.svg"}
        alt={`${name} flag`}
        width={300}
        height={200}
        className="w-full h-48 object-cover"
      />
      <div className="p-4">
        <h2 className="text-xl font-semibold mb-2">{name}</h2>
        <Link
          href={`/languages/${id}`}
          className="bg-skin-button-primary text-skin-inverted px-4 py-2 rounded transition-colors"
        >
          Play!
        </Link>
      </div>
    </div>
  );
}

function validatePropsOrDie(
  props: LanguageCardProps
): asserts props is LanguageCardProps {
  assert(props.id.trim().length > 0, "id is required");
  assert(props.name.trim().length > 0, "name is required");
}
