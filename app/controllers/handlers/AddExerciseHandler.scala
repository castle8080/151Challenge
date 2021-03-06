package controllers.handlers

import controllers.Application._
import forms.{ExerciseEntriesForm, ExerciseEntryFormInput}
import models.{ExerciseEntries, ExerciseEntry}
import play.api.data.{Form, FormError}
import play.api.mvc.{AnyContent, Request, Result}
import utils.authentication.UserAuthenticator

object AddExerciseHandler {
  def addExerciseEntry(implicit request: Request[AnyContent]): Result = {
    val formInfo: Form[ExerciseEntryFormInput] = ExerciseEntriesForm.exerciseEntryForm.bindFromRequest()
    if (formInfo.errors.nonEmpty) {
      BadRequest {
        val formErrorString = formInfo.errors.foldLeft("")((aggregateString: String, eachError: FormError) => {
          aggregateString + " " + eachError.message
        })
        "There were problems with your submission: " + formErrorString
      }
    } else {
      val formData = formInfo.get
      UserAuthenticator.getAuthenticatedUser(request).fold(BadRequest("No authenticated user when adding exercise")){
        authUser => {
          //ID is 0 here because it does not have an affect on insertion
          val exerciseEntry = ExerciseEntry(0, formData.exerciseType, formData.reps, formData.when, authUser.id, formData.comment)
          val errorString = ExerciseEntriesForm.validateEntry(exerciseEntry)
          errorString.fold {
            ExerciseEntries.add(exerciseEntry)
            Redirect("/displayEntries")
          }(BadRequest(_))
        }
      }
    }
  }

}
