import { useState, useEffect } from 'react';
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend, Area, AreaChart,
} from 'recharts';
import { Zap, Droplets, TrendingUp, TrendingDown, Minus } from 'lucide-react';
import { PageHeader } from '@/components/shared/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import { EmptyState } from '@/components/shared/EmptyState';

type Period = '6M' | '1Y' | '2Y';

interface ConsumptionPoint {
  month: string;
  electricity: number;
  water: number;
}

// Realistic simulated consumption data for Madagascar
const MOCK_CONSUMPTION: ConsumptionPoint[] = [
  { month: 'Jan', electricity: 245, water: 18 },
  { month: 'Fév', electricity: 232, water: 20 },
  { month: 'Mar', electricity: 228, water: 22 },
  { month: 'Avr', electricity: 210, water: 19 },
  { month: 'Mai', electricity: 198, water: 16 },
  { month: 'Juin', electricity: 215, water: 17 },
  { month: 'Juil', electricity: 220, water: 15 },
  { month: 'Aoû', electricity: 235, water: 14 },
  { month: 'Sep', electricity: 240, water: 16 },
  { month: 'Oct', electricity: 250, water: 18 },
  { month: 'Nov', electricity: 255, water: 21 },
  { month: 'Déc', electricity: 260, water: 23 },
];

function filterByPeriod(data: ConsumptionPoint[], period: Period): ConsumptionPoint[] {
  switch (period) {
    case '6M': return data.slice(-6);
    case '1Y': return data.slice(-12);
    case '2Y': return data;
  }
}

function computeStats(data: ConsumptionPoint[]) {
  if (data.length === 0) return null;
  const avgElec = data.reduce((s, p) => s + p.electricity, 0) / data.length;
  const avgWater = data.reduce((s, p) => s + p.water, 0) / data.length;
  const last = data[data.length - 1];
  const prev = data.length > 1 ? data[data.length - 2] : null;
  const elecChange = prev && last ? ((last.electricity - prev.electricity) / prev.electricity * 100) : 0;
  const waterChange = prev && last ? ((last.water - prev.water) / prev.water * 100) : 0;
  const totalElec = data.reduce((s, p) => s + p.electricity, 0);
  const totalWater = data.reduce((s, p) => s + p.water, 0);

  return { avgElec, avgWater, elecChange, waterChange, totalElec, totalWater, last, prev };
}

function TrendBadge({ value, suffix }: { value: number; suffix: string }) {
  const abs = Math.abs(value);
  return (
    <span className={`inline-flex items-center gap-1 text-sm font-medium ${
      Math.abs(value) < 1 ? 'text-muted-foreground' : value > 0 ? 'text-energy' : 'text-success'
    }`}>
      {Math.abs(value) < 1 ? <Minus className="h-4 w-4" /> : value > 0 ? <TrendingUp className="h-4 w-4" /> : <TrendingDown className="h-4 w-4" />}
      {abs.toFixed(1)}{suffix}
    </span>
  );
}

function CustomTooltip({ active, payload, label }: any) {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded-lg border border-border bg-card p-3 shadow-lg">
      <p className="text-sm font-medium text-foreground mb-1">{label}</p>
      {payload.map((entry: any) => (
        <p key={entry.name} className="text-sm" style={{ color: entry.color }}>
          {entry.name === 'electricity' ? 'Électricité' : 'Eau'} : {entry.value} {entry.name === 'electricity' ? 'kWh' : 'm³'}
        </p>
      ))}
    </div>
  );
}

export function ConsumptionPage() {
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState<Period>('1Y');

  useEffect(() => {
    // Simulate loading delay — replace with API call later
    const timer = setTimeout(() => setLoading(false), 600);
    return () => clearTimeout(timer);
  }, []);

  const filteredData = filterByPeriod(MOCK_CONSUMPTION, period);
  const stats = computeStats(filteredData);

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center">
        <LoadingSpinner size="lg" label="Chargement des données de consommation…" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Ma consommation"
        description="Suivez votre consommation d'électricité et d'eau"
      />

      {/* Period selector */}
      <div className="flex gap-2">
        {(['6M', '1Y', '2Y'] as Period[]).map(p => (
          <button
            key={p}
            onClick={() => setPeriod(p)}
            className={`rounded-lg px-4 py-1.5 text-sm font-medium transition-colors ${
              period === p
                ? 'bg-primary text-primary-foreground'
                : 'bg-secondary text-muted-foreground hover:bg-secondary/80'
            }`}
          >
            {p === '6M' ? '6 mois' : p === '1Y' ? '1 an' : '2 ans'}
          </button>
        ))}
      </div>

      {/* Summary Cards */}
      {stats && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-xs font-medium text-muted-foreground">Moyenne Électricité</p>
                  <p className="text-2xl font-bold text-foreground">{stats.avgElec.toFixed(0)}</p>
                  <p className="text-xs text-muted-foreground">kWh / mois</p>
                </div>
                <div className="rounded-lg bg-primary/10 p-2">
                  <Zap className="h-5 w-5 text-primary" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-xs font-medium text-muted-foreground">Total Électricité</p>
                  <p className="text-2xl font-bold text-foreground">{stats.totalElec.toFixed(0)}</p>
                  <p className="text-xs text-muted-foreground">kWh</p>
                </div>
                <TrendBadge value={stats.elecChange} suffix="%" />
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-xs font-medium text-muted-foreground">Moyenne Eau</p>
                  <p className="text-2xl font-bold text-foreground">{stats.avgWater.toFixed(1)}</p>
                  <p className="text-xs text-muted-foreground">m³ / mois</p>
                </div>
                <div className="rounded-lg bg-energy/10 p-2">
                  <Droplets className="h-5 w-5 text-energy" />
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-xs font-medium text-muted-foreground">Total Eau</p>
                  <p className="text-2xl font-bold text-foreground">{stats.totalWater.toFixed(1)}</p>
                  <p className="text-xs text-muted-foreground">m³</p>
                </div>
                <TrendBadge value={stats.waterChange} suffix="%" />
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* Electricity Chart */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Zap className="h-5 w-5 text-primary" />
            Consommation d'électricité
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={filteredData}>
                <defs>
                  <linearGradient id="elecGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0057B8" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#0057B8" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis
                  dataKey="month"
                  tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }}
                  axisLine={{ stroke: 'hsl(var(--border))' }}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }}
                  axisLine={false}
                  tickLine={false}
                  unit=" kWh"
                />
                <Tooltip content={<CustomTooltip />} />
                <Area
                  type="monotone"
                  dataKey="electricity"
                  stroke="#0057B8"
                  strokeWidth={2}
                  fill="url(#elecGradient)"
                  name="electricity"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Water Chart */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Droplets className="h-5 w-5 text-energy" />
            Consommation d'eau
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={filteredData}>
                <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" />
                <XAxis
                  dataKey="month"
                  tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }}
                  axisLine={{ stroke: 'hsl(var(--border))' }}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 12, fill: 'hsl(var(--muted-foreground))' }}
                  axisLine={false}
                  tickLine={false}
                  unit=" m³"
                />
                <Tooltip content={<CustomTooltip />} />
                <Bar
                  dataKey="water"
                  fill="#F59E0B"
                  radius={[4, 4, 0, 0]}
                  maxBarSize={40}
                  name="water"
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </CardContent>
      </Card>

      {/* Comparison note */}
      <Card>
        <CardContent className="p-4 text-center">
          <p className="text-sm text-muted-foreground">
            Les données de consommation sont mises à jour mensuellement après relevé des compteurs.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}
