import { ArrowRight, Zap, Droplets, Shield, Smartphone } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { APP } from '@/lib/constants';
import { motion } from 'framer-motion';

const features = [
  {
    icon: Zap,
    title: 'Électricité',
    description: 'Gérez votre compteur électrique et suivez votre consommation en temps réel.',
    color: 'bg-energy-50 text-energy-700 dark:bg-energy-950 dark:text-energy-300',
  },
  {
    icon: Droplets,
    title: 'Eau',
    description: 'Accédez à vos données de consommation d\'eau et gérez vos abonnements.',
    color: 'bg-primary/10 text-primary',
  },
  {
    icon: Shield,
    title: 'Sécurisé',
    description: 'Plateforme sécurisée pour vos paiements et données personnelles.',
    color: 'bg-success/10 text-success',
  },
  {
    icon: Smartphone,
    title: 'Mobile',
    description: 'Accédez à vos services depuis votre smartphone, où que vous soyez.',
    color: 'bg-purple-50 text-purple-700 dark:bg-purple-950 dark:text-purple-300',
  },
];

const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.15 },
  },
};

const itemVariants = {
  hidden: { opacity: 0, y: 30 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.6, ease: 'easeOut' } },
};

export function HomePage() {
  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-primary via-primary-700 to-primary-900 py-24 lg:py-32">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute -left-40 -top-40 h-96 w-96 rounded-full bg-white blur-3xl" />
          <div className="absolute -right-40 -bottom-40 h-96 w-96 rounded-full bg-energy-400 blur-3xl" />
        </div>

        <div className="container relative mx-auto max-w-6xl px-4">
          <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            className="mx-auto max-w-3xl text-center"
          >
            <motion.h1
              variants={itemVariants}
              className="text-4xl font-bold tracking-tight text-white sm:text-5xl lg:text-6xl"
            >
              Bienvenue sur le portail{' '}
              <span className="text-energy-400">{APP.fullName}</span>
            </motion.h1>

            <motion.p
              variants={itemVariants}
              className="mt-6 text-lg leading-relaxed text-white/80"
            >
              Consultez vos factures, suivez votre consommation, signalez des incidents
              et communiquez avec JIRAMA en toute simplicité.
            </motion.p>

            <motion.div
              variants={itemVariants}
              className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row"
            >
              <Link to="/login">
                <Button size="lg" className="bg-white text-primary hover:bg-white/90 shadow-xl">
                  Se connecter
                  <ArrowRight className="ml-2 h-5 w-5" />
                </Button>
              </Link>
              <Link to="/register">
                <Button
                  size="lg"
                  variant="outline"
                  className="border-white/30 text-white hover:bg-white/10 hover:text-white"
                >
                  Créer un compte
                </Button>
              </Link>
            </motion.div>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20">
        <div className="container mx-auto max-w-6xl px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.6 }}
            className="text-center"
          >
            <h2 className="text-3xl font-bold text-foreground">
              Nos services
            </h2>
            <p className="mt-4 text-lg text-muted-foreground">
              Tout ce dont vous avez besoin pour gérer vos services JIRAMA
            </p>
          </motion.div>

          <motion.div
            variants={containerVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="mt-12 grid gap-8 sm:grid-cols-2 lg:grid-cols-4"
          >
            {features.map((feature) => (
              <motion.div
                key={feature.title}
                variants={itemVariants}
                className="group rounded-xl border border-border bg-card p-6 transition-all hover:shadow-card-hover hover:-translate-y-1"
              >
                <div className={`inline-flex rounded-lg p-3 ${feature.color}`}>
                  <feature.icon className="h-6 w-6" />
                </div>
                <h3 className="mt-4 text-lg font-semibold text-foreground">
                  {feature.title}
                </h3>
                <p className="mt-2 text-sm text-muted-foreground leading-relaxed">
                  {feature.description}
                </p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-gradient-to-r from-primary-800 to-primary-700 py-16">
        <div className="container mx-auto max-w-4xl px-4 text-center">
          <h2 className="text-3xl font-bold text-white">
            Besoin d'aide ?
          </h2>
          <p className="mt-4 text-lg text-white/70">
            Contactez notre service client au <strong className="text-energy-400">100</strong>
          </p>
        </div>
      </section>
    </div>
  );
}
