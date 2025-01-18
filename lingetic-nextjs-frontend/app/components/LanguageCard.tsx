import Image from 'next/image';
import Link from 'next/link';

interface LanguageCardProps {
  id: string
  name: string
  description: string
  image?: string
};

const LanguageCard: React.FC<LanguageCardProps> = ({ id, name, description, image }) => {
  return (
    <div className="bg-white shadow-lg rounded-lg overflow-hidden">
      <Image src={image ?? "/img/languages/placeholder.svg"} alt={`${name} flag`} width={300} height={200} className="w-full h-48 object-cover" />
      <div className="p-4">
        <h2 className="text-xl font-semibold mb-2">{name}</h2>
        <p className="text-gray-600 mb-4">{description}</p>
        <Link href={`/languages/${id}`} className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors">
          Play!
        </Link>
      </div>
    </div>
  );
}

export default LanguageCard;
