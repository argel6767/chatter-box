import { MessageCircle, Users, Zap } from "lucide-react";
import { Card, CardContent } from "./ui/card"

const features = [
    {
      icon: MessageCircle,
      title: "Real-time Messaging",
      description: "Instant communication with lightning-fast message delivery and real-time updates."
    },
    {
      icon: Users,
      title: "Group Conversations",
      description: "Create and manage multiple chat rooms for different topics and communities."
    },
    {
      icon: Zap,
      title: "Lightning Fast",
      description: "Optimized for speed with minimal latency and seamless user experience."
    },
  ];

export const FeatureSection = () => {


    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center mb-16 animate-fade-in">
          <h2 className="text-3xl sm:text-4xl font-bold text-white mb-4">
            Powerful Features
          </h2>
          <p className="text-xl text-gray-300">
            Everything you need for seamless communication
          </p>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {features.map((feature, index) => (
            <Card 
              key={index} 
              className="bg-white/5 border-white/10 backdrop-blur-sm hover:bg-white/10 transition-all duration-300 group animate-slide-in"
              style={{ animationDelay: `${index * 0.1}s` }}
            >
              <CardContent className="p-6 text-center">
                <feature.icon className="h-12 w-12 text-slate-400 mx-auto mb-4 group-hover:scale-110 transition-transform" />
                <h3 className="text-xl font-semibold text-white mb-2">
                  {feature.title}
                </h3>
                <p className="text-gray-300">
                  {feature.description}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>   
    )
} 