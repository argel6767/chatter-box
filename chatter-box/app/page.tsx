import { Navbar } from "@/components/navbar";
import { Footer } from "@/components/footer";
import { FeatureSection } from "@/components/feature-section";
import { Hero } from "@/components/hero";

const Index = () => {

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Navigation */}
      <Navbar />
      {/* Hero Section */}
      <section className="relative overflow-hidden py-20 sm:py-32">
        <Hero/>
      </section>
      <section className="py-20 bg-black/20 backdrop-blur-sm motion-translate-x-in-[0%] motion-translate-y-in-[52%] motion-ease-spring-smooth">
        <FeatureSection />
      </section>
      <footer className="py-8">
        <Footer/>
      </footer>
    </div>
  );
};

export default Index;