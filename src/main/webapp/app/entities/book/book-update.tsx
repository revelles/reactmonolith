import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate, ICrudGetAction, ICrudGetAllAction, ICrudPutAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { IAuthor } from 'app/shared/model/author.model';
import { getEntities as getAuthors } from 'app/entities/author/author.reducer';
import { getEntity, updateEntity, createEntity, reset } from './book.reducer';
import { IBook } from 'app/shared/model/book.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IBookUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const BookUpdate = (props: IBookUpdateProps) => {
  const [authorId, setAuthorId] = useState('0');
  const [isNew, setIsNew] = useState(!props.match.params || !props.match.params.id);

  const { bookEntity, authors, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/book');
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }

    props.getAuthors();
  }, []);

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...bookEntity,
        ...values,
      };

      if (isNew) {
        props.createEntity(entity);
      } else {
        props.updateEntity(entity);
      }
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="reactmonolithApp.book.home.createOrEditLabel">
            <Translate contentKey="reactmonolithApp.book.home.createOrEditLabel">Create or edit a Book</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : bookEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="book-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="book-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="titleLabel" for="book-title">
                  <Translate contentKey="reactmonolithApp.book.title">Title</Translate>
                </Label>
                <AvField id="book-title" type="text" name="title" />
              </AvGroup>
              <AvGroup>
                <Label id="descriptionLabel" for="book-description">
                  <Translate contentKey="reactmonolithApp.book.description">Description</Translate>
                </Label>
                <AvField id="book-description" type="text" name="description" />
              </AvGroup>
              <AvGroup>
                <Label id="publicationDateLabel" for="book-publicationDate">
                  <Translate contentKey="reactmonolithApp.book.publicationDate">Publication Date</Translate>
                </Label>
                <AvField id="book-publicationDate" type="date" className="form-control" name="publicationDate" />
              </AvGroup>
              <AvGroup>
                <Label id="priceLabel" for="book-price">
                  <Translate contentKey="reactmonolithApp.book.price">Price</Translate>
                </Label>
                <AvField id="book-price" type="text" name="price" />
              </AvGroup>
              <AvGroup>
                <Label for="book-author">
                  <Translate contentKey="reactmonolithApp.book.author">Author</Translate>
                </Label>
                <AvInput id="book-author" type="select" className="form-control" name="author.id">
                  <option value="" key="0" />
                  {authors
                    ? authors.map(otherEntity => (
                        <option value={otherEntity.id} key={otherEntity.id}>
                          {otherEntity.name}
                        </option>
                      ))
                    : null}
                </AvInput>
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/book" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </AvForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = (storeState: IRootState) => ({
  authors: storeState.author.entities,
  bookEntity: storeState.book.entity,
  loading: storeState.book.loading,
  updating: storeState.book.updating,
  updateSuccess: storeState.book.updateSuccess,
});

const mapDispatchToProps = {
  getAuthors,
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(BookUpdate);
