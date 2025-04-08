import HeroSection from "./components/landing-page/HeroSection";
import { languages } from "./languages/constants";

export default function Home() {
  return (
    <main className="min-h-screen">
      <HeroSection languages={languages} />
    </main>
  );
}
