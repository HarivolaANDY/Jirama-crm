import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import fr from './locales/fr.json';
import mg from './locales/mg.json';

const resources = {
  fr: { translation: fr },
  mg: { translation: mg },
} as const;

i18n.use(initReactI18next).init({
  resources,
  lng: 'fr',       // Default: French
  fallbackLng: 'fr',
  interpolation: {
    escapeValue: false, // React already escapes values
  },
});

export default i18n;
