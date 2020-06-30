import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, ICrudGetAction, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './author.reducer';
import { IAuthor } from 'app/shared/model/author.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IAuthorDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const AuthorDetail = (props: IAuthorDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { authorEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2>
          <Translate contentKey="reactmonolithApp.author.detail.title">Author</Translate> [<b>{authorEntity.id}</b>]
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="name">
              <Translate contentKey="reactmonolithApp.author.name">Name</Translate>
            </span>
          </dt>
          <dd>{authorEntity.name}</dd>
          <dt>
            <span id="birthDate">
              <Translate contentKey="reactmonolithApp.author.birthDate">Birth Date</Translate>
            </span>
          </dt>
          <dd>
            {authorEntity.birthDate ? <TextFormat value={authorEntity.birthDate} type="date" format={APP_LOCAL_DATE_FORMAT} /> : null}
          </dd>
        </dl>
        <Button tag={Link} to="/author" replace color="info">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/author/${authorEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ author }: IRootState) => ({
  authorEntity: author.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(AuthorDetail);
