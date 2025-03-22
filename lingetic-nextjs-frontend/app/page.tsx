import {
  ArrowRight,
  MessageSquare,
  BarChart2,
  PenTool,
  Globe,
} from "lucide-react";
import { LanguageProperty } from "./languages/page";
import React from "react";

const languages = [
  {
    id: "English",
    name: "English",
    description: "Learn English, the most widely spoken language in the world.",
    image: undefined,
  },
  {
    id: "Turkish",
    name: "Turkish",
    description:
      "Learn Turkish, the official language of Turkey and a gateway to rich cultural heritage.",
    image: undefined,
  },
];

export default function Home() {
  return (
    <main className="min-h-screen">
      <HeroSection languages={languages} />
      <FeaturesSection />
      <PhilosophySection />
      <TestimonialsSection />
      <CTASection />
      <FooterSection />
    </main>
  );
}

function HeroSection({ languages }: { languages: LanguageProperty[] }) {
  return (
    <section className="relative h-screen flex flex-col items-center justify-center px-4 md:px-8 lg:px-24">
      <div className="absolute inset-0 -z-10 bg-gradient-to-b from-blue-50 to-white" />
      <div className="max-w-4xl mx-auto text-center">
        <h1 className="text-5xl md:text-7xl font-bold mb-6 text-[#374151] tracking-tight">
          Learn Languages for <span className="text-[#2563eb]">Real-World</span>{" "}
          Conversations
        </h1>
        <p className="text-xl md:text-2xl mb-10 text-[#374151] max-w-2xl mx-auto">
          AI-powered language learning that prepares you to speak confidently
          with native speakers.
        </p>
        <LanguageList languages={languages} />
      </div>
      <div className="absolute bottom-10 left-0 right-0 flex justify-center animate-bounce">
        <ArrowRight className="rotate-90 h-8 w-8 text-[#2563eb]" />
      </div>
    </section>
  );
}

function LanguageList({ languages }: { languages: LanguageProperty[] }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl mx-auto">
      {languages.map((language) => (
        <LanguageCard key={language.id} language={language} />
      ))}
    </div>
  );
}

function LanguageCard({ language }: { language: LanguageProperty }) {
  return (
    <div className="flex flex-col md:flex-row items-center p-4 border border-gray-100 hover:shadow-lg transition-shadow duration-300 cursor-pointer">
      <div className="w-24 h-24 rounded-full overflow-hidden flex-shrink-0 mb-4 md:mb-0 md:mr-4">
        <img
          src={language.image || "/placeholder.svg?height=96&width=96"}
          alt={language.name}
          className="w-full h-full object-cover"
        />
      </div>
      <div className="flex-1 text-left">
        <h3 className="text-xl font-semibold mb-1 text-[#374151]">
          {language.name}
        </h3>
        <p className="text-sm text-[#374151]">{language.description}</p>
      </div>
    </div>
  );
}

function FeaturesSection() {
  return (
    <section className="py-20 px-4 md:px-8 lg:px-24 bg-white">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-4xl font-bold mb-4 text-[#374151]">
            Key Features
          </h2>
          <p className="text-lg text-[#374151] max-w-2xl mx-auto">
            Designed to get you conversing with native speakers as quickly as
            possible
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <FeatureCard
            icon={<MessageSquare className="h-10 w-10 text-[#2563eb]" />}
            title="Practical Scenarios"
            description="Learn language through everyday situations like ordering food, asking for directions, or making small talk."
          />
          <FeatureCard
            icon={<Globe className="h-10 w-10 text-[#2563eb]" />}
            title="AI Conversations"
            description="Practice speaking with AI actors who adapt to your proficiency level."
          />
          <FeatureCard
            icon={<PenTool className="h-10 w-10 text-[#2563eb]" />}
            title="Writing Exercises"
            description="Compose letters or messages to imaginary penpals, with AI-powered feedback."
          />
          <FeatureCard
            icon={<BarChart2 className="h-10 w-10 text-[#2563eb]" />}
            title="Progress Tracking"
            description="Monitor your improvement and unlock new scenarios as you advance."
          />
        </div>
      </div>
    </section>
  );
}

function FeatureCard({
  icon,
  title,
  description,
}: {
  icon: React.ReactElement;
  title: string;
  description: string;
}) {
  return (
    <div className="p-6 border border-gray-100 hover:shadow-lg transition-shadow duration-300">
      <div className="mb-4">{icon}</div>
      <h3 className="text-xl font-semibold mb-2 text-[#374151]">{title}</h3>
      <p className="text-[#374151]">{description}</p>
    </div>
  );
}

function PhilosophySection() {
  return (
    <section className="py-20 px-4 md:px-8 lg:px-24 bg-blue-50">
      <div className="max-w-7xl mx-auto flex flex-col lg:flex-row items-center gap-12">
        <div className="lg:w-1/2">
          <img
            src="/placeholder.svg?height=500&width=600"
            alt="People conversing in different languages"
            className="rounded-lg shadow-xl"
          />
        </div>
        <div className="lg:w-1/2">
          <h2 className="text-3xl md:text-4xl font-bold mb-6 text-[#374151]">
            Our Philosophy
          </h2>
          <p className="text-lg mb-6 text-[#374151]">
            Lingetic is built on the belief that the best way to learn a
            language is through interaction with native speakers. This app
            doesn't aim to replace these crucial real-world interactions.
          </p>
          <p className="text-lg mb-8 text-[#374151]">
            Instead, it prepares you to make the most of them by equipping you
            with the confidence and skills to get started.
          </p>
          <button className="bg-[#2563eb] hover:bg-blue-700 text-[#ffffff]">
            Learn More About Our Approach
          </button>
        </div>
      </div>
    </section>
  );
}

function TestimonialsSection() {
  return (
    <section className="py-20 px-4 md:px-8 lg:px-24 bg-white">
      <div className="max-w-7xl mx-auto">
        <div className="text-center mb-16">
          <h2 className="text-3xl md:text-4xl font-bold mb-4 text-[#374151]">
            What Our Users Say
          </h2>
          <p className="text-lg text-[#374151] max-w-2xl mx-auto">
            Join thousands of language learners who have improved their
            conversation skills
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <TestimonialCard
            quote="After just 3 weeks with Lingetic, I was able to order food and ask for directions on my trip to Spain!"
            name="Sarah K."
            role="Spanish Learner"
          />
          <TestimonialCard
            quote="The AI conversations feel so natural. I practice every day and my confidence has skyrocketed."
            name="Michael T."
            role="French Learner"
          />
          <TestimonialCard
            quote="I've tried many language apps, but Lingetic is the only one that prepared me for real conversations."
            name="Aisha M."
            role="Japanese Learner"
          />
        </div>
      </div>
    </section>
  );
}

function TestimonialCard({
  quote,
  name,
  role,
}: {
  quote: string;
  name: string;
  role: string;
}) {
  return (
    <div className="p-8 border border-gray-100 hover:shadow-lg transition-shadow duration-300">
      <div className="mb-6 text-[#2563eb]">
        <svg
          width="45"
          height="36"
          viewBox="0 0 45 36"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M13.5 0H0V13.5C0 20.9558 6.04416 27 13.5 27V36C6.04416 36 0 29.9558 0 22.5V18H13.5V0ZM40.5 0H27V13.5C27 20.9558 33.0442 27 40.5 27V36C33.0442 36 27 29.9558 27 22.5V18H40.5V0Z"
            fill="currentColor"
          />
        </svg>
      </div>
      <p className="text-lg mb-6 text-[#374151]">{quote}</p>
      <div>
        <p className="font-semibold text-[#374151]">{name}</p>
        <p className="text-sm text-gray-500">{role}</p>
      </div>
    </div>
  );
}

function CTASection() {
  return (
    <section className="py-20 px-4 md:px-8 lg:px-24 bg-gradient-to-r from-blue-600 to-blue-700">
      <div className="max-w-4xl mx-auto text-center">
        <h2 className="text-3xl md:text-4xl font-bold mb-6 text-white">
          Ready to Start Your Language Journey?
        </h2>
        <p className="text-xl mb-10 text-white opacity-90">
          Join Lingetic today and start having real conversations in your target
          language.
        </p>
        <button className="bg-white text-[#2563eb] hover:bg-blue-50 px-8 py-6 text-lg">
          Get Started Free
          <ArrowRight className="ml-2 h-5 w-5" />
        </button>
      </div>
    </section>
  );
}

function FooterSection() {
  return (
    <footer className="py-12 px-4 md:px-8 lg:px-24 bg-gray-900 text-white">
      <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
        <div>
          <h3 className="text-xl font-bold mb-4">Lingetic</h3>
          <p className="text-gray-300">
            AI-Powered Language Learning for Real-World Conversations
          </p>
        </div>
        <div>
          <h4 className="text-lg font-semibold mb-4">Product</h4>
          <ul className="space-y-2 text-gray-300">
            <li>Features</li>
            <li>Pricing</li>
            <li>Languages</li>
            <li>Enterprise</li>
          </ul>
        </div>
        <div>
          <h4 className="text-lg font-semibold mb-4">Resources</h4>
          <ul className="space-y-2 text-gray-300">
            <li>Blog</li>
            <li>Community</li>
            <li>Support</li>
            <li>FAQ</li>
          </ul>
        </div>
        <div>
          <h4 className="text-lg font-semibold mb-4">Company</h4>
          <ul className="space-y-2 text-gray-300">
            <li>About Us</li>
            <li>Careers</li>
            <li>Contact</li>
            <li>Privacy Policy</li>
          </ul>
        </div>
      </div>
      <div className="max-w-7xl mx-auto mt-12 pt-8 border-t border-gray-800 text-center text-gray-400">
        <p>© {new Date().getFullYear()} Lingetic. All rights reserved.</p>
      </div>
    </footer>
  );
}
