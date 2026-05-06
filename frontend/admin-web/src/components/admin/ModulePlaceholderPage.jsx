import PageHero from "./PageHero";

export default function ModulePlaceholderPage({ moduleName, description }) {
  return (
    <section className="page-shell">
      <PageHero
        badges={["Admin module", moduleName]}
        title={moduleName}
        description={description}
      />
    </section>
  );
}
