import Image from "next/image";
import Link from "next/link";

interface LanguageCardProps {
  id: string;
  name: string;
  description: string;
  image?: string;
}

const LanguageCard: React.FC<LanguageCardProps> = ({
  id,
  name,
  description,
  image,
}) => {
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
        <p className="text-skin-base mb-4">{description}</p>
        <Link
          href={`/languages/${id}`}
          className="bg-skin-button-primary text-skin-inverted px-4 py-2 rounded transition-colors"
        >
          Play!
        </Link>
      </div>
    </div>
  );
};

export default LanguageCard;
