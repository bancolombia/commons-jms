import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Easy to Use',
    Svg: require('@site/static/img/feature-first.svg').default,
    description: (
      <>
        Commons JMS comes with starter setup to start quickly and is annotation driven.
      </>
    ),
  },
  {
    title: 'Focus on Domain',
    Svg: require('@site/static/img/feature-second.svg').default,
    description: (
      <>
        Commons JMS lets you focus on your domain solution delegating JMS concerns and performance to the library.
      </>
    ),
  },
  {
    title: 'Imperative and Reactive Programming',
    Svg: require('@site/static/img/feature-third.svg').default,
    description: (
      <>
        Get the reactive programming benefits out of the box.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
