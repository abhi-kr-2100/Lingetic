import HeroSection from "./components/landing-page/HeroSection";
import WakeUpBackend from "./components/landing-page/WakeUpBackend";
import { languages } from "./languages/constants";

export default function Home() {
  return (
    <div className="flex-1">
      <HeroSection languages={languages} />
      <WakeUpBackend />
    </div>
  );
}
