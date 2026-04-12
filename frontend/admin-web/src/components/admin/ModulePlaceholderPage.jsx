import EndpointPanel from "./EndpointPanel";
import PageHero from "./PageHero";

export default function ModulePlaceholderPage({
  moduleName,
  description,
  endpoints,
}) {
  return (
    <section className="page-shell">
      <PageHero
        badges={["Admin module", moduleName]}
        title={moduleName}
        description={description}
      />

      <EndpointPanel title={`${moduleName} endpoints`} endpoints={endpoints} />
    </section>
  );
}
