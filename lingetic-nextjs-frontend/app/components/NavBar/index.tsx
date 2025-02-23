import Link from "next/link";
import AuthButton from "./AuthButton";

const Navbar = () => {
  return (
    <nav className="bg-skin-button-primary p-4">
      <div className="container mx-auto flex justify-between items-center">
        <Link href="/" className="text-skin-inverted text-2xl font-bold">
          Lingetic
        </Link>
        <div className="space-x-4">
          <Link href="/languages" className="text-skin-inverted">
            Languages
          </Link>
          <AuthButton />
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
